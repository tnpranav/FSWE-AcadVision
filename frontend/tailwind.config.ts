import type { Config } from 'tailwindcss'

export default {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        campus: {
          navy: '#0f172a',
          indigo: '#4f46e5',
          brand: '#6366f1',
          light: '#f8fafc',
          dark: '#0b0f19',
          card: '#ffffff',
          darkCard: '#1e293b',
          border: '#e2e8f0',
          darkBorder: '#334155',
          muted: '#64748b',
        },
      },
      fontFamily: {
        sans: ['DM Sans', 'sans-serif'],
        display: ['Manrope', 'sans-serif'],
      },
    },
  },
  plugins: [],
} satisfies Config
