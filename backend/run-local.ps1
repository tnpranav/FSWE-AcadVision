$ErrorActionPreference = 'Stop'

# Keep local provider credentials out of source control. Values already set in
# the shell take precedence over backend/.env.local.
$localEnvFile = Join-Path $PSScriptRoot '.env.local'
if (Test-Path $localEnvFile) {
  Get-Content $localEnvFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith('#') -and $line.Contains('=')) {
      $parts = $line -split '=', 2
      $name = $parts[0].Trim()
      $value = $parts[1].Trim().Trim('"').Trim("'")
      if ($name -and -not [Environment]::GetEnvironmentVariable($name, 'Process')) {
        Set-Item -Path "Env:$name" -Value $value
      }
    }
  }
}

$jdk = 'C:\Program Files\Java\jdk-21.0.12'
if (Test-Path (Join-Path $jdk 'bin\java.exe')) {
  $env:JAVA_HOME = $jdk
}

if (-not $env:JWT_SECRET) {
  $keyBytes = New-Object byte[] 48
  $random = [Security.Cryptography.RandomNumberGenerator]::Create()
  $random.GetBytes($keyBytes)
  $random.Dispose()
  $env:JWT_SECRET = [Convert]::ToBase64String($keyBytes)
  Write-Host 'Generated a temporary JWT secret for this local run.'
}

if (-not $env:DB_URL) { $env:DB_URL = 'jdbc:postgresql://localhost:5432/acadvision' }
if (-not $env:DB_USERNAME) { $env:DB_USERNAME = 'acadvision' }
if (-not $env:DB_PASSWORD) { $env:DB_PASSWORD = 'acadvision_dev' }
if (-not $env:FRONTEND_ORIGIN) { $env:FRONTEND_ORIGIN = 'http://localhost:5173' }
$env:JAVA_TOOL_OPTIONS = "-Duser.home=$PSScriptRoot\.maven-home"
Set-Location $PSScriptRoot
& .\mvnw.cmd spring-boot:run
exit $LASTEXITCODE
