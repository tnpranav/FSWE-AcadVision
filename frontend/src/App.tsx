import { Fragment, useEffect, useMemo, useRef, useState } from 'react'
import {
  Activity, AlarmClock, ArrowDownRight, ArrowUpRight, Bell, BookOpen, CalendarDays,
  Check, ChevronDown, ChevronLeft, ChevronRight, CircleDollarSign, ClipboardCheck,
  Clock3, Download, GraduationCap, LayoutDashboard, LogOut, Menu, Moon, MoreHorizontal,
  Plus, Search, Settings, ShieldCheck, Sun, Users, Wallet, X, FileText, UserRound,
  Sparkles, PanelRight, BookMarked, CircleHelp, Filter, CheckCircle2, AlertCircle
} from 'lucide-react'
import {
  Area, AreaChart, Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart,
  ResponsiveContainer, Tooltip, XAxis, YAxis
} from 'recharts'
import { apiRequest, USE_API } from './api'

type Role = 'Admin' | 'Faculty' | 'Student'
type Student = { id: string; name: string; email: string; department: string; semester: number; cgpa: number; attendance: number; status: string; hobbies: string }
type Faculty = { id: string; name: string; email: string; department: string; courses: number; status: string }
type Course = { id: string; title: string; code: string; faculty: string; students: number; completion: number; color: string; moduleCount?:number; completedModules?:number; attendedSessions?:number; sessions?:number; sectionCode?:string; enrollmentStatus?:string; credits?:number }
type Notice = { title: string; detail: string; time: string; tone: string }
type PortalSnapshot = { profile?:any; attendance?:any[]; grades?:any[]; gpaHistory?:any[]; fees?:any[]; timetable?:any[]; assignments?:any[]; notifications?:any[]; supportTickets?:any[] }
type RazorpayCheckoutResult = { razorpay_order_id:string; razorpay_payment_id:string; razorpay_signature:string }
type RazorpayCheckout = { open:()=>void; on:(event:string, callback:(result:any)=>void)=>void }
declare global { interface Window { Razorpay?: new(options:any)=>RazorpayCheckout } }

function loadRazorpayCheckout():Promise<void> {
  if (window.Razorpay) return Promise.resolve()
  return new Promise((resolve,reject) => {
    let script=document.querySelector<HTMLScriptElement>('script[data-razorpay-checkout]')
    if (!script) {
      script=document.createElement('script'); script.src='https://checkout.razorpay.com/v1/checkout.js'; script.async=true
      script.dataset.razorpayCheckout='true'; document.body.appendChild(script)
    }
    script.addEventListener('load',()=>window.Razorpay?resolve():reject(new Error('Razorpay Checkout failed to initialize.')),{once:true})
    script.addEventListener('error',()=>{script?.remove();reject(new Error('Could not load Razorpay Checkout. Check your internet connection.'))},{once:true})
    if (window.Razorpay) resolve()
  })
}

const initialStudents: Student[] = [
  { id:'AV24001', name:'Aarav Sharma', email:'aarav.sharma@campus.edu', department:'Computer Science', semester:5, cgpa:8.72, attendance:92, status:'Active', hobbies:'Photography, Chess' },
  { id:'AV24002', name:'Diya Patel', email:'diya.patel@campus.edu', department:'Information Technology', semester:5, cgpa:9.14, attendance:96, status:'Active', hobbies:'Reading, Badminton' },
  { id:'AV24003', name:'Ishaan Reddy', email:'ishaan.reddy@campus.edu', department:'Artificial Intelligence', semester:5, cgpa:7.68, attendance:73, status:'At risk', hobbies:'Robotics, Cricket' },
  { id:'AV24004', name:'Meera Nair', email:'meera.nair@campus.edu', department:'Computer Science', semester:3, cgpa:8.91, attendance:89, status:'Active', hobbies:'Music, Painting' },
  { id:'AV24005', name:'Kabir Verma', email:'kabir.verma@campus.edu', department:'Data Science', semester:7, cgpa:8.26, attendance:81, status:'Active', hobbies:'Travel, Coding' },
]
const initialFaculty: Faculty[] = [
  { id:'FAC1001', name:'Dr. Ananya Rao', email:'ananya.rao@campus.edu', department:'Computer Science', courses:3, status:'Active' },
  { id:'FAC1002', name:'Prof. Vikram Das', email:'vikram.das@campus.edu', department:'Mathematics', courses:2, status:'Active' },
  { id:'FAC1003', name:'Dr. Sana Iqbal', email:'sana.iqbal@campus.edu', department:'Artificial Intelligence', courses:4, status:'Active' },
]
const initialCourses: Course[] = [
  { id:'CS501', title:'Data Structures & Algorithms', code:'CS501', faculty:'Dr. Ananya Rao', students:68, completion:78, color:'#7065f0' },
  { id:'AI520', title:'Machine Learning', code:'AI520', faculty:'Dr. Sana Iqbal', students:54, completion:64, color:'#28b89b' },
  { id:'DB510', title:'Database Systems', code:'DB510', faculty:'Prof. Vikram Das', students:72, completion:86, color:'#f5a544' },
  { id:'WD505', title:'Full Stack Development', code:'WD505', faculty:'Dr. Ananya Rao', students:48, completion:51, color:'#ef7b9a' },
]
const initialNotices: Notice[] = [
  { title:'Mid-semester examinations', detail:'Exam timetable is now available for all departments.', time:'10 min ago', tone:'purple' },
  { title:'Course registration window', detail:'Registration closes on Friday at 5:00 PM.', time:'1 hour ago', tone:'orange' },
  { title:'Attendance reminder', detail:'Students below 75% attendance should contact their faculty advisor.', time:'3 hours ago', tone:'green' },
]
const semesterData = [{name:'Sem 1',cgpa:7.4},{name:'Sem 2',cgpa:7.8},{name:'Sem 3',cgpa:8.1},{name:'Sem 4',cgpa:8.0},{name:'Sem 5',cgpa:8.7}]
const attendanceData = [{name:'Mon',present:88,absent:12},{name:'Tue',present:93,absent:7},{name:'Wed',present:84,absent:16},{name:'Thu',present:96,absent:4},{name:'Fri',present:90,absent:10}]
const progressData = [{name:'Completed',value:72},{name:'Remaining',value:28}]
const palette = ['#7065f0','#28b89b','#f5a544','#ef7b9a','#6da7f7']

export default function App({ authenticatedRole, authenticatedUser, onLogout }: { authenticatedRole?: 'ADMIN'|'FACULTY'|'STUDENT'; authenticatedUser?: { userId: string; fullName: string; role: 'ADMIN'|'FACULTY'|'STUDENT' }; onLogout?: () => void }) {
  const [role, setRole] = useState<Role>(authenticatedRole === 'FACULTY' ? 'Faculty' : authenticatedRole === 'STUDENT' ? 'Student' : 'Admin')
  const [page, setPage] = useState('Overview')
  const [students, setStudents] = useState<Student[]>(initialStudents)
  const [faculty, setFaculty] = useState<Faculty[]>(initialFaculty)
  const [courses, setCourses] = useState<Course[]>(initialCourses)
  const [notices, setNotices] = useState<Notice[]>(initialNotices)
  const [query, setQuery] = useState('')
  const [drawer, setDrawer] = useState<{title:string; kind:string; data?:any} | null>(null)
  const [dark, setDark] = useState(false)
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [noticeOpen, setNoticeOpen] = useState(false)
  const [profileMenuOpen, setProfileMenuOpen] = useState(false)
  const profileMenuRef = useRef<HTMLDivElement>(null)
  const [toast, setToast] = useState('')
  const [studentForm, setStudentForm] = useState({id:'',name:'',email:'',department:'Computer Science',semester:1,cgpa:0,attendance:100,status:'Active',hobbies:''})
  const [facultyForm, setFacultyForm] = useState({id:'',name:'',email:'',department:'Computer Science',courses:0,status:'Active'})
  const [provisionPassword, setProvisionPassword] = useState('')
  const [attendance, setAttendance] = useState<Record<string,string>>({})
  const [courseIndex, setCourseIndex] = useState(0)
  const [portal, setPortal] = useState<PortalSnapshot>({})
  const [paymentBusy, setPaymentBusy] = useState<string|null>(null)
  const [availableCourses, setAvailableCourses] = useState<any[]>([])
  const [assignmentForm, setAssignmentForm] = useState({code:'',title:'',description:'',totalMarks:100,weightage:10,dueDate:''})
  const [submissionText, setSubmissionText] = useState('')
  const [announcementForm, setAnnouncementForm] = useState({title:'',content:'',audience:'ALL'})
  const [accounts, setAccounts] = useState<any[]>([])
  const [accountForm, setAccountForm] = useState({userId:'',fullName:'',email:'',role:'STUDENT',initialPassword:'',department:'Computer Science'})
  const [supportForm, setSupportForm] = useState({category:'ACADEMIC',subject:'',description:'',priority:'MEDIUM'})

  const fallbackAccount = role === 'Admin'
    ? {id:'admin',name:'Alex Morgan',department:'Administration'}
    : role === 'Faculty'
      ? {id:'FAC1001',name:'Dr. Ananya Rao',department:'Computer Science'}
      : {id:'AV24001',name:'Aarav Sharma',department:'Computer Science'}
  const account = {
    id: authenticatedUser?.userId || portal.profile?.id || fallbackAccount.id,
    name: authenticatedUser?.fullName || portal.profile?.name || fallbackAccount.name,
    department: portal.profile?.department || fallbackAccount.department,
  }
  const avatarInitials = account.name.replace(/^(Dr\.|Prof\.|Professor)\s+/i, '').split(/\s+/).filter(Boolean).slice(0, 2).map((part:string) => part[0]).join('').toUpperCase()

  useEffect(() => {
    if (!profileMenuOpen) return
    const closeOnOutsideClick = (event: PointerEvent) => {
      if (!profileMenuRef.current?.contains(event.target as Node)) setProfileMenuOpen(false)
    }
    const closeOnEscape = (event: KeyboardEvent) => { if (event.key === 'Escape') setProfileMenuOpen(false) }
    document.addEventListener('pointerdown', closeOnOutsideClick)
    document.addEventListener('keydown', closeOnEscape)
    return () => {
      document.removeEventListener('pointerdown', closeOnOutsideClick)
      document.removeEventListener('keydown', closeOnEscape)
    }
  }, [profileMenuOpen])

  useEffect(() => {
    if (USE_API) return
    const saved = localStorage.getItem('acadvision-state')
    if (saved) {
      try {
        const parsed = JSON.parse(saved)
        setStudents(parsed.students || initialStudents)
        setFaculty(parsed.faculty || initialFaculty)
        setCourses(parsed.courses || initialCourses)
        setNotices(parsed.notices || initialNotices)
      } catch { /* keep demo data */ }
    }
  }, [])
  useEffect(() => {
    if (USE_API) return
    localStorage.setItem('acadvision-state', JSON.stringify({students, faculty, courses, notices}))
  }, [students, faculty, courses, notices])
  useEffect(() => {
    if (!USE_API) return
    let active = true
    if (role !== 'Admin') {
      Promise.all([apiRequest<any>('/portal/me'),apiRequest<any>(role==='Student'?'/student/profile':'/faculty/profile'),role==='Student'?apiRequest<any[]>('/student/courses'):Promise.resolve([])]).then(([data,profile,catalog]) => {
        if (!active) return
        data.profile={...data.profile,...profile}
        setPortal(data)
        if(role==='Student') setAvailableCourses(catalog)
        if (data.profile) {
          if (role === 'Student') setStudents([{id:data.profile.id,name:data.profile.name,email:data.profile.email,department:data.profile.department,semester:data.profile.semester,cgpa:Number(data.profile.cgpa),attendance:Number(data.profile.attendance),status:data.profile.status,hobbies:data.profile.hobbies||''}])
          else setFaculty([{id:data.profile.id,name:data.profile.name,email:data.profile.email,department:data.profile.department,courses:(data.courses||[]).length,status:'Active'}])
        }
        setCourses((data.courses||[]).map((row:any) => ({id:row.code,title:row.title,code:row.code,faculty:row.faculty||'',students:Number(row.students||0),completion:Number(row.completion||0),color:palette[(data.courses||[]).indexOf(row)%palette.length],moduleCount:Number(row.module_count||0),completedModules:Number(row.completed_modules||0),attendedSessions:Number(row.attended_sessions||0),sessions:Number(row.sessions||0)})))
        if (role === 'Faculty') setStudents(Array.from(new Map((data.students||[]).map((row:any) => [row.id,{id:row.id,name:row.name,email:row.email,department:row.department,semester:Number(row.semester),cgpa:Number(row.cgpa),attendance:Number(row.attendance),status:row.status,hobbies:row.hobbies||''}])).values()) as Student[])
        setNotices((data.announcements||[]).map((row:any) => ({title:row.title,detail:row.detail,time:new Date(row.time).toLocaleString(),tone:row.priority==='URGENT'||row.priority==='HIGH'?'orange':'purple'})))
      }).catch(error => { if(active) showToast(`Could not load your portal data: ${error.message}`) })
      return () => { active = false }
    }
    Promise.all([
      apiRequest<any[]>('/admin/students'), apiRequest<any[]>('/admin/faculty'), apiRequest<any[]>('/admin/courses'),
      apiRequest<any[]>('/faculty/attendance'), apiRequest<any[]>('/announcements'), apiRequest<any[]>('/admin/users'),apiRequest<any[]>('/notifications'),apiRequest<any[]>('/support'),
    ]).then(([studentRows, facultyRows, courseRows, attendanceRows, noticeRows, accountRows, notificationRows, supportRows]) => {
      if (!active) return
      setStudents(studentRows.map(row => ({id:row.studentId,name:row.fullName,email:row.email,department:row.department,semester:row.semester,cgpa:Number(row.cgpa),attendance:Number(row.attendance),status:row.status,hobbies:row.hobbies||''})))
      setFaculty(facultyRows.map(row => ({id:row.facultyId,name:row.fullName,email:row.email,department:row.department,courses:row.courseCount,status:row.status})))
      setCourses(courseRows.map(row => ({id:row.courseCode,title:row.title,code:row.courseCode,faculty:row.facultyName,students:row.enrolledStudents,completion:row.completion,color:row.color})))
      setPortal(prev=>({...prev,attendance:attendanceRows}))
      setNotices(noticeRows.map(row=>({title:row.title,detail:row.content,time:new Date(row.created_at).toLocaleString(),tone:row.priority==='URGENT'||row.priority==='HIGH'?'orange':'purple'})))
      setAccounts(accountRows)
      setPortal(prev=>({...prev,notifications:notificationRows}))
      setPortal(prev=>({...prev,supportTickets:supportRows}))
    }).catch(error => { if (active) showToast(`Could not load records: ${error.message}`) })
    return () => { active = false }
  }, [role])
  useEffect(() => {
    if (!toast) return
    const timer = window.setTimeout(() => setToast(''), 2800)
    return () => window.clearTimeout(timer)
  }, [toast])

  const nav = useMemo(() => {
    if (role === 'Admin') return [
      {label:'Workspace',items:[['Overview',LayoutDashboard],['Students',GraduationCap],['Faculty',Users],['Courses',BookOpen],['Enrollments',ClipboardCheck]]},
      {label:'Operations',items:[['Attendance',CalendarDays],['Fees & payments',Wallet],['Reports',Activity],['Announcements',Bell],['Support',CircleHelp]]},
      {label:'System',items:[['Access & roles',ShieldCheck],['Settings',Settings]]},
    ]
    if (role === 'Faculty') return [
      {label:'Teaching',items:[['Overview',LayoutDashboard],['My courses',BookOpen],['My students',Users],['Attendance',ClipboardCheck],['Assignments',FileText]]},
      {label:'Schedule',items:[['Timetable',CalendarDays],['Announcements',Bell],['Support',CircleHelp],['My profile',UserRound]]},
    ]
    return [
      {label:'My learning',items:[['Overview',LayoutDashboard],['My courses',BookOpen],['Course registration',BookMarked],['Academic progress',Activity],['Attendance',ClipboardCheck]]},
      {label:'Campus life',items:[['Timetable',CalendarDays],['Fees & payments',Wallet],['Assignments',FileText],['Announcements',Bell],['Support',CircleHelp],['My profile',UserRound]]},
    ]
  }, [role])

  const showToast = (message:string) => setToast(message)
  const changeRole = (newRole:Role) => { setRole(newRole); setPage('Overview'); setDrawer(null); setQuery('') }
  const filteredStudents = students.filter(s => `${s.id} ${s.name} ${s.email} ${s.department}`.toLowerCase().includes(query.toLowerCase()))
  const filteredFaculty = faculty.filter(s => `${s.id} ${s.name} ${s.email} ${s.department}`.toLowerCase().includes(query.toLowerCase()))
  const filteredCourses = courses.filter(s => `${s.id} ${s.title} ${s.faculty}`.toLowerCase().includes(query.toLowerCase()))

  const addStudent = async () => {
    if (!studentForm.id.trim() || !studentForm.name.trim() || !studentForm.email.includes('@')) { showToast('Enter a student ID, name, and valid email.'); return }
    if (USE_API && provisionPassword.length < 12) { showToast('Set an initial password with at least 12 characters.'); return }
    if (students.some(s => s.id === studentForm.id)) { showToast('That student ID already exists.'); return }
    const input = {studentId:studentForm.id,fullName:studentForm.name,email:studentForm.email,department:studentForm.department,semester:Number(studentForm.semester),cgpa:Number(studentForm.cgpa),attendance:Number(studentForm.attendance),status:studentForm.status,hobbies:studentForm.hobbies,initialPassword:provisionPassword}
    try { if (USE_API) await apiRequest('/admin/students',{method:'POST',body:JSON.stringify(input)}) }
    catch(error) { showToast(error instanceof Error?error.message:'Could not save student.'); return }
    setStudents(prev => [{...studentForm, semester:Number(studentForm.semester),cgpa:Number(studentForm.cgpa),attendance:Number(studentForm.attendance)}, ...prev])
    setDrawer(null); setStudentForm({id:'',name:'',email:'',department:'Computer Science',semester:1,cgpa:0,attendance:100,status:'Active',hobbies:''}); setProvisionPassword('')
    showToast(USE_API?'Student and login account created. The user must change the initial password at sign-in.':'Student registered successfully.')
  }
  const addFaculty = async () => {
    if (!facultyForm.id.trim() || !facultyForm.name.trim() || !facultyForm.email.includes('@')) { showToast('Enter a faculty ID, name, and valid email.'); return }
    if (USE_API && provisionPassword.length < 12) { showToast('Set an initial password with at least 12 characters.'); return }
    if (faculty.some(s => s.id === facultyForm.id)) { showToast('That faculty ID already exists.'); return }
    try { if (USE_API) await apiRequest('/admin/faculty',{method:'POST',body:JSON.stringify({facultyId:facultyForm.id,fullName:facultyForm.name,email:facultyForm.email,department:facultyForm.department,courseCount:Number(facultyForm.courses),status:facultyForm.status,initialPassword:provisionPassword})}) }
    catch(error) { showToast(error instanceof Error?error.message:'Could not save faculty.'); return }
    setFaculty(prev => [{...facultyForm,courses:Number(facultyForm.courses)}, ...prev])
    setDrawer(null); setFacultyForm({id:'',name:'',email:'',department:'Computer Science',courses:0,status:'Active'}); setProvisionPassword('')
    showToast(USE_API?'Faculty and login account created. The user must change the initial password at sign-in.':'Faculty account created.')
  }
  const deleteStudent = async (id:string) => { try { if(USE_API) await apiRequest(`/admin/students/${id}`,{method:'DELETE'}); setStudents(prev => prev.map(s=>s.id===id?{...s,status:'Inactive'}:s)); showToast(USE_API?'Student account deactivated.':'Student removed from active demo records.') } catch(error) { showToast(error instanceof Error?error.message:'Could not deactivate student.') } }
  const deleteFaculty = async (id:string) => { try { if(USE_API) await apiRequest(`/admin/faculty/${id}`,{method:'DELETE'}); setFaculty(prev => prev.map(s=>s.id===id?{...s,status:'Inactive'}:s)); showToast(USE_API?'Faculty account deactivated.':'Faculty removed from active demo records.') } catch(error) { showToast(error instanceof Error?error.message:'Could not deactivate faculty.') } }
  const exportCsv = (kind:'students'|'faculty') => {
    const rows:any[] = kind === 'students' ? students : faculty
    if (!rows.length) return
    const keys = Object.keys(rows[0])
    const csv = [keys.join(','), ...rows.map(row => keys.map(k => JSON.stringify(row[k] ?? '')).join(','))].join('\\n')
    const url = URL.createObjectURL(new Blob([csv], {type:'text/csv'}))
    const a = document.createElement('a'); a.href = url; a.download = `acadvision-${kind}.csv`; a.click(); URL.revokeObjectURL(url)
    showToast(`${kind} exported as CSV.`)
  }
  const openStudentForm = (student?:Student) => {
    setProvisionPassword('')
    setStudentForm(student ? {...student} : {id:'',name:'',email:'',department:'Computer Science',semester:1,cgpa:0,attendance:100,status:'Active',hobbies:''})
    setDrawer({title:student ? 'Edit student' : 'Register student',kind:student ? 'editStudent' : 'newStudent',data:student})
  }
  const openFacultyForm = (member?:Faculty) => {
    setProvisionPassword('')
    setFacultyForm(member ? {...member} : {id:'',name:'',email:'',department:'Computer Science',courses:0,status:'Active'})
    setDrawer({title:member ? 'Edit faculty' : 'Add faculty',kind:member ? 'editFaculty' : 'newFaculty',data:member})
  }
  const saveEditedStudent = async () => {
    if (!studentForm.name.trim() || !studentForm.email.includes('@')) { showToast('Enter a name and valid email.'); return }
    const input = {studentId:studentForm.id,fullName:studentForm.name,email:studentForm.email,department:studentForm.department,semester:Number(studentForm.semester),cgpa:Number(studentForm.cgpa),attendance:Number(studentForm.attendance),status:studentForm.status,hobbies:studentForm.hobbies}
    try { if(USE_API) await apiRequest(`/admin/students/${drawer?.data?.id}`,{method:'PUT',body:JSON.stringify(input)}) }
    catch(error) { showToast(error instanceof Error?error.message:'Could not update student.'); return }
    setStudents(prev => prev.map(s => s.id === drawer?.data?.id ? {...studentForm, semester:Number(studentForm.semester),cgpa:Number(studentForm.cgpa),attendance:Number(studentForm.attendance)} : s))
    setDrawer(null); showToast('Student record updated.')
  }
  const saveEditedFaculty = async () => {
    if (!facultyForm.name.trim() || !facultyForm.email.includes('@')) { showToast('Enter a name and valid email.'); return }
    try { if(USE_API) await apiRequest(`/admin/faculty/${drawer?.data?.id}`,{method:'PUT',body:JSON.stringify({facultyId:facultyForm.id,fullName:facultyForm.name,email:facultyForm.email,department:facultyForm.department,courseCount:Number(facultyForm.courses),status:facultyForm.status})}) }
    catch(error) { showToast(error instanceof Error?error.message:'Could not update faculty.'); return }
    setFaculty(prev => prev.map(s => s.id === drawer?.data?.id ? {...facultyForm,courses:Number(facultyForm.courses)} : s))
    setDrawer(null); showToast('Faculty record updated.')
  }

  const renderOverview = () => (
    <>
      <div className="welcome-banner">
        <div className="welcome-copy"><div className="eyebrow"><Sparkles size={14}/> YOUR CAMPUS, IN ONE VIEW</div><h2>{role === 'Admin' ? 'Good evening, Admin!' : role === 'Faculty' ? 'Welcome back, Professor!' : 'Your learning journey, at a glance.'}</h2><p>{role === 'Admin' ? 'Here’s what’s happening across AcadVision today.' : role === 'Faculty' ? 'Your courses and students are ready for you.' : 'Keep learning, keep growing. You’re doing great.'}</p><button className="button white" onClick={() => setPage(role === 'Admin' ? 'Reports' : role === 'Faculty' ? 'My courses' : 'Academic progress')}>Explore insights <ChevronRight size={16}/></button></div>
        <div className="banner-art"><div className="art-orbit orbit-one"></div><div className="art-orbit orbit-two"></div><div className="art-cap"><GraduationCap size={76}/></div><span className="float-chip chip-a"><span className="mini-dot mint"></span> Learning +12%</span><span className="float-chip chip-b"><Sparkles size={14}/> Looking good!</span></div>
      </div>
      <div className="stats-grid">
        {(role === 'Admin' ? [
          {label:'Total students',value:students.length.toLocaleString(),delta:'+8.2%',icon:GraduationCap,tone:'lavender',note:'vs. last semester'},
          {label:'Faculty members',value:faculty.length.toLocaleString(),delta:'+2.4%',icon:Users,tone:'mint',note:'active teaching staff'},
          {label:'Active courses',value:courses.length.toLocaleString(),delta:'+4.6%',icon:BookOpen,tone:'peach',note:'this semester'},
          {label:'Avg. attendance',value:`${Math.round(students.reduce((a,s)=>a+s.attendance,0)/(students.length||1))}%`,delta:'+3.1%',icon:CalendarDays,tone:'pink',note:'across student records'},
        ] : role === 'Faculty' ? [
          {label:'Assigned courses',value:String(USE_API?courses.length:4),delta:'',icon:BookOpen,tone:'lavender',note:'this semester'},
          {label:'Enrolled students',value:String(USE_API?new Set(students.map(s=>s.id)).size:242),delta:'',icon:Users,tone:'mint',note:'across your courses'},
          {label:'Avg. attendance',value:USE_API&&students.length?`${Math.round(students.reduce((sum,s)=>sum+s.attendance,0)/students.length)}%`:'89%',delta:'',icon:CalendarDays,tone:'peach',note:'your course sections'},
          {label:'To be graded',value:String(USE_API?(portal.assignments||[]).reduce((sum:any,a:any)=>sum+Number(a.pending_count||0),0):18),delta:'',icon:ClipboardCheck,tone:'pink',note:'submissions pending'},
        ] : [
          {label:'Current CGPA',value:USE_API?Number(portal.profile?.cgpa||0).toFixed(2):'8.72',delta:'',icon:Activity,tone:'lavender',note:'cumulative grade point average'},
          {label:'Enrolled courses',value:String(USE_API?courses.length:5),delta:'',icon:BookOpen,tone:'mint',note:'current semester'},
          {label:'Attendance',value:USE_API?`${Number(portal.profile?.attendance||0).toFixed(0)}%`:'92%',delta:'',icon:CalendarDays,tone:'peach',note:'overall attendance'},
          {label:'Credits earned',value:USE_API?`${portal.profile?.credits||0}/${portal.profile?.credits_required||120}`:'84/120',delta:'',icon:GraduationCap,tone:'pink',note:'degree progress'},
        ]).map((s,i) => <div className="stat-card" key={s.label}><div className={`stat-icon ${s.tone}`}><s.icon size={21}/></div><div className="stat-label">{s.label}</div><div className="stat-value">{s.value}</div><div className="stat-foot"><span className="positive"><ArrowUpRight size={14}/>{s.delta}</span><span>{s.note}</span></div><div className={`stat-spark spark-${i}`}></div></div>)}
      </div>
      <div className="content-grid two-one">
        <section className="panel chart-panel"><div className="panel-heading"><div><h3>{role === 'Student' ? 'Your CGPA journey' : 'Academic performance'}</h3><p>{role === 'Student' ? 'A little progress adds up.' : 'A snapshot of student performance over time.'}</p></div><button className="select-pill">This semester <ChevronDown size={14}/></button></div><div className="chart-area"><ResponsiveContainer width="100%" height="100%"><AreaChart data={semesterData} margin={{top:12,right:8,left:-20,bottom:0}}><defs><linearGradient id="cgpaFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#7567f7" stopOpacity={0.25}/><stop offset="100%" stopColor="#7567f7" stopOpacity={0.01}/></linearGradient></defs><CartesianGrid strokeDasharray="4 5" vertical={false} stroke="var(--line)"/><XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill:'var(--muted)',fontSize:12}}/><YAxis domain={[6,10]} axisLine={false} tickLine={false} tick={{fill:'var(--muted)',fontSize:12}}/><Tooltip contentStyle={{borderRadius:12,border:'1px solid var(--line)',background:'var(--panel)',color:'var(--text)'}}/><Area type="monotone" dataKey="cgpa" stroke="#7567f7" strokeWidth={3} fill="url(#cgpaFill)" activeDot={{r:5}}/></AreaChart></ResponsiveContainer></div><div className="chart-legend"><span><i className="legend-dot purple"></i> CGPA trend</span><span className="legend-insight"><ArrowUpRight size={14}/> Consistent progress</span></div></section>
        <section className="panel"><div className="panel-heading"><div><h3>Today’s schedule</h3><p>Monday · Your upcoming classes</p></div><button className="icon-button" onClick={() => setPage('Timetable')}><MoreHorizontal size={19}/></button></div><div className="schedule-list">{[{time:'09:00',period:'AM',title:'Data Structures',room:'Room B-204',tone:'purple'},{time:'11:00',period:'AM',title:'Machine Learning',room:'Lab 3 · Block A',tone:'mint'},{time:'02:00',period:'PM',title:'Database Systems',room:'Room C-110',tone:'orange'}].map((item,i)=><div className="schedule-item" key={item.title}><div className="schedule-time"><b>{item.time}</b><span>{item.period}</span></div><div className={`schedule-mark ${item.tone}`}></div><div className="schedule-info"><b>{item.title}</b><span><Clock3 size={12}/>{item.room}</span></div><button className="tiny-arrow" onClick={() => setDrawer({title:item.title,kind:'schedule',data:item})}><ChevronRight size={16}/></button></div>)}</div><button className="text-action" onClick={() => setPage('Timetable')}>View full timetable <ArrowUpRight size={15}/></button></section>
      </div>
      <div className="content-grid two-one lower-grid">
        <section className="panel"><div className="panel-heading"><div><h3>{role === 'Admin' ? 'Course completion' : 'Your course progress'}</h3><p>Keep an eye on the learning journey.</p></div><button className="text-action" onClick={() => setPage(role === 'Admin' ? 'Courses' : role === 'Faculty' ? 'My courses' : 'My courses')}>View all <ChevronRight size={15}/></button></div><div className="course-mini-grid">{courses.slice(0,3).map((c,i)=><button className="course-mini" key={c.id} onClick={() => setDrawer({title:c.title,kind:'course',data:c})}><div className="course-mini-top"><span className="course-symbol" style={{background:`${c.color}18`,color:c.color}}><BookOpen size={18}/></span><span className="course-code">{c.code}</span></div><b>{c.title}</b><div className="mini-progress"><span style={{width:`${c.completion}%`,background:c.color}}></span></div><div className="mini-progress-foot"><span>{c.completion}% complete</span><span>{c.students} students</span></div></button>)}</div></section>
        <section className="panel notices-panel"><div className="panel-heading"><div><h3>Campus updates</h3><p>Don’t miss what’s new.</p></div><span className="notice-count">{notices.length}</span></div><div className="notice-list">{notices.slice(0,3).map(n=><button className="notice-item" key={n.title} onClick={() => setDrawer({title:n.title,kind:'notice',data:n})}><span className={`notice-symbol ${n.tone}`}><Bell size={16}/></span><span className="notice-copy"><b>{n.title}</b><span>{n.detail}</span><small>{n.time}</small></span></button>)}</div><button className="text-action" onClick={() => setPage('Announcements')}>All announcements <ChevronRight size={15}/></button></section>
      </div>
    </>
  )

  const renderStudents = () => <section className="panel table-panel"><div className="panel-heading"><div><h3>Student directory</h3><p>Manage student profiles, academic details, and status.</p></div><div className="table-actions"><button className="button secondary" onClick={() => exportCsv('students')}><Download size={15}/> Export</button><button className="button primary" onClick={() => openStudentForm()}><Plus size={16}/> Add student</button></div></div><div className="table-toolbar"><div className="table-search"><Search size={16}/><input placeholder="Search by name, ID, department..." value={query} onChange={e=>setQuery(e.target.value)}/></div><button className="button secondary small" onClick={()=>setQuery('')}><Filter size={14}/> Clear filter</button><span className="record-count">{filteredStudents.length} records</span></div><div className="table-scroll"><table><thead><tr><th>Student</th><th>Student ID</th><th>Department</th><th>Semester</th><th>CGPA</th><th>Attendance</th><th>Status</th><th></th></tr></thead><tbody>{filteredStudents.map(s=><tr key={s.id}><td><button className="person-cell" onClick={()=>setDrawer({title:s.name,kind:'studentDetail',data:s})}><span className="avatar student-avatar">{s.name.split(' ').map(n=>n[0]).slice(0,2).join('')}</span><span><b>{s.name}</b><small>{s.email}</small></span></button></td><td><span className="id-tag">{s.id}</span></td><td>{s.department}</td><td>Semester {s.semester}</td><td><b>{s.cgpa.toFixed(2)}</b></td><td><div className="attendance-cell"><span className="mini-progress"><span style={{width:`${s.attendance}%`,background:s.attendance<75?'#ef7b9a':'#28b89b'}}></span></span><span>{s.attendance}%</span></div></td><td><span className={`status ${s.status==='At risk'?'risk':'active'}`}><i/>{s.status}</span></td><td><button className="icon-button" onClick={()=>setDrawer({title:s.name,kind:'studentActions',data:s})}><MoreHorizontal size={18}/></button></td></tr>)}</tbody></table></div>{filteredStudents.length===0&&<div className="empty-state"><Users size={30}/><b>No students found</b><span>Try another search term or add a student.</span></div>}</section>

  const renderFaculty = () => <section className="panel table-panel"><div className="panel-heading"><div><h3>Faculty directory</h3><p>Manage teaching staff and course assignments.</p></div><div className="table-actions"><button className="button secondary" onClick={()=>exportCsv('faculty')}><Download size={15}/> Export</button><button className="button primary" onClick={()=>openFacultyForm()}><Plus size={16}/> Add faculty</button></div></div><div className="table-toolbar"><div className="table-search"><Search size={16}/><input placeholder="Search faculty by name, ID..." value={query} onChange={e=>setQuery(e.target.value)}/></div><span className="record-count">{filteredFaculty.length} records</span></div><div className="table-scroll"><table><thead><tr><th>Faculty member</th><th>Faculty ID</th><th>Department</th><th>Courses</th><th>Status</th><th></th></tr></thead><tbody>{filteredFaculty.map(f=><tr key={f.id}><td><button className="person-cell" onClick={()=>setDrawer({title:f.name,kind:'facultyDetail',data:f})}><span className="avatar faculty-avatar">{f.name.replace('Dr. ','').replace('Prof. ','').split(' ').map(n=>n[0]).slice(0,2).join('')}</span><span><b>{f.name}</b><small>{f.email}</small></span></button></td><td><span className="id-tag">{f.id}</span></td><td>{f.department}</td><td><span className="course-number">{f.courses} courses</span></td><td><span className="status active"><i/>{f.status}</span></td><td><button className="icon-button" onClick={()=>setDrawer({title:f.name,kind:'facultyActions',data:f})}><MoreHorizontal size={18}/></button></td></tr>)}</tbody></table></div></section>

  const renderMyStudents = () => <section className="panel table-panel"><div className="panel-heading"><div><h3>Students in my courses</h3><p>Only students enrolled in courses assigned to your faculty account.</p></div><button className="button secondary" onClick={()=>exportCsv('students')}><Download size={15}/> Export</button></div><div className="table-toolbar"><div className="table-search"><Search size={16}/><input placeholder="Search by name or student ID..." value={query} onChange={e=>setQuery(e.target.value)}/></div><span className="record-count">{filteredStudents.length} students</span></div><div className="table-scroll"><table><thead><tr><th>Student</th><th>ID</th><th>Department</th><th>Semester</th><th>CGPA</th><th>Attendance</th></tr></thead><tbody>{filteredStudents.map(s=><tr key={s.id}><td><button className="person-cell" onClick={()=>setDrawer({title:s.name,kind:'studentDetail',data:s})}><span className="avatar student-avatar">{s.name.split(' ').map(n=>n[0]).slice(0,2).join('')}</span><span><b>{s.name}</b><small>{s.email}</small></span></button></td><td><span className="id-tag">{s.id}</span></td><td>{s.department}</td><td>{s.semester}</td><td>{s.cgpa.toFixed(2)}</td><td>{s.attendance}%</td></tr>)}</tbody></table></div>{!filteredStudents.length&&<div className="empty-state"><Users size={28}/><b>No enrolled students found</b><span>Students appear when they enroll in your assigned courses.</span></div>}</section>

  const renderAccounts = () => <section className="panel table-panel"><div className="panel-heading"><div><h3>User accounts</h3><p>Manage student and faculty access. Passwords are never shown.</p></div><button className="button primary" onClick={()=>{setAccountForm({userId:'',fullName:'',email:'',role:'STUDENT',initialPassword:'',department:'Computer Science'});setDrawer({title:'Create account',kind:'newAccount'})}}><Plus size={16}/> Create account</button></div><div className="table-toolbar"><div className="table-search"><Search size={16}/><input placeholder="Search ID, name, email..." value={query} onChange={e=>setQuery(e.target.value)}/></div><span className="record-count">{accounts.length} accounts</span></div><div className="table-scroll"><table><thead><tr><th>Name</th><th>Account ID</th><th>Role</th><th>Last login</th><th>Status</th><th>Security</th><th></th></tr></thead><tbody>{accounts.filter(a=>`${a.userId} ${a.fullName} ${a.email}`.toLowerCase().includes(query.toLowerCase())).map(a=><tr key={a.userId}><td><b>{a.fullName}</b><small className="table-subtext">{a.email}</small></td><td><span className="id-tag">{a.userId}</span></td><td>{a.role}</td><td>{a.lastLoginAt?new Date(a.lastLoginAt).toLocaleString():'Never'}</td><td>{a.active?'Active':'Inactive'}</td><td>{a.mustChangePassword?'Password update required':'Current'}</td><td><button className="button secondary small" onClick={()=>setAccountStatus(a)}>{a.active?'Deactivate':'Activate'}</button>{a.active&&!a.mustChangePassword&&<button className="text-action" onClick={()=>requirePasswordChange(a)}>Require password change</button>}</td></tr>)}</tbody></table></div></section>

  const renderCourses = () => <><div className="course-toolbar"><div><h3>Explore courses</h3><p>Monitor completion, student engagement, and course delivery.</p></div><div className="table-search"><Search size={16}/><input placeholder="Search courses..." value={query} onChange={e=>setQuery(e.target.value)}/></div></div><div className="course-grid">{filteredCourses.map((c,i)=><section className="panel course-card" key={c.id}><div className="course-card-top"><span className="course-symbol large" style={{background:`${c.color}18`,color:c.color}}><BookMarked size={21}/></span><button className="icon-button" onClick={()=>setDrawer({title:c.title,kind:'course',data:c})}><MoreHorizontal size={18}/></button></div><span className="id-tag">{c.code}</span><h3>{c.title}</h3><p className="course-faculty"><UserRound size={14}/>{c.faculty}</p><div className="donut-wrap"><ResponsiveContainer width="100%" height={150}><PieChart><Pie data={[{name:'Complete',value:c.completion},{name:'Remaining',value:100-c.completion}]} dataKey="value" cx="50%" cy="50%" innerRadius={46} outerRadius={61} startAngle={90} endAngle={-270} stroke="none">{[0,1].map((n)=><Cell key={n} fill={n===0?c.color:'var(--soft)'}/>)}</Pie><Tooltip/></PieChart></ResponsiveContainer><div className="donut-center"><b>{c.completion}%</b><span>complete</span></div></div><div className="course-card-foot"><span><Users size={15}/>{role==='Student'?'Enrolled':`${c.students} students`}</span><button className="text-action" onClick={()=>setDrawer({title:c.title,kind:'course',data:c})}>Details <ChevronRight size={14}/></button></div></section>)}</div></>

  const renderRegistration = () => <section className="panel table-panel"><div className="panel-heading"><div><h3>Course registration</h3><p>Registration is checked on the server for window, capacity, prerequisites, duplicates, and timetable conflicts.</p></div></div><div className="course-grid">{availableCourses.map((row:any,index:number)=><article className="panel course-card" key={`${row.code}-${row.section_code||index}`}><span className="id-tag">{row.code}</span><h3>{row.title}</h3><p>{row.department} · {row.credits} credits</p><p className="course-faculty">{row.faculty||'Faculty to be assigned'} · Section {row.section_code||'not scheduled'}</p><p>{row.enrollment_status||'Not enrolled'} · {row.enrolled_count||0}/{row.max_capacity||'—'} seats</p>{row.section_code&&row.enrollment_status!=='ENROLLED'&&<button className="button primary full" onClick={()=>enrollInCourse(row)}>Register</button>}</article>)}</div>{!availableCourses.length&&<div className="empty-state"><BookOpen size={28}/><b>No course sections open</b><span>Check again when the registration window opens.</span></div>}</section>

  const saveAttendance = async () => {
    if (!courses.length) { showToast('There are no assigned courses to take attendance for.'); return }
    if (USE_API) {
      try {
        await Promise.all(students.map(s => apiRequest('/faculty/attendance',{method:'POST',body:JSON.stringify({studentId:s.id,courseCode:courses[courseIndex%courses.length].code,classDate:new Date().toISOString().slice(0,10),status:(attendance[s.id]||'Present').toUpperCase()})})))
        showToast('Attendance submitted and saved.')
      } catch(error) { showToast(error instanceof Error?error.message:'Attendance could not be saved.') }
    } else showToast('Attendance register saved in the current demo session.')
  }
  const createAssignment = async () => {
    if (!assignmentForm.code || !assignmentForm.title.trim() || !assignmentForm.dueDate) { showToast('Select a course, title, and due date.'); return }
    try {
      await apiRequest('/faculty/assignments',{method:'POST',body:JSON.stringify({courseCode:assignmentForm.code,title:assignmentForm.title,description:assignmentForm.description,totalMarks:Number(assignmentForm.totalMarks),weightage:Number(assignmentForm.weightage),dueDate:new Date(assignmentForm.dueDate).toISOString()})})
      const data=await apiRequest<any[]>('/faculty/assignments'); setPortal(prev=>({...prev,assignments:data})); setDrawer(null); showToast('Assignment created.')
    } catch(error) { showToast(error instanceof Error?error.message:'Could not create assignment.') }
  }
  const submitAssignment = async () => {
    if (!submissionText.trim() || !drawer?.data?.id) { showToast('Add your submission before saving.'); return }
    try {
      await apiRequest(`/student/assignments/${drawer.data.id}/submit`,{method:'POST',body:JSON.stringify({content:submissionText})})
      const data=await apiRequest<any[]>('/student/assignments'); setPortal(prev=>({...prev,assignments:data})); setDrawer(null); setSubmissionText(''); showToast('Assignment submitted.')
    } catch(error) { showToast(error instanceof Error?error.message:'Could not submit assignment.') }
  }
  const payInvoice = async (invoice:any) => {
    if (paymentBusy) return
    setPaymentBusy(invoice.id)
    try {
      const order=await apiRequest<any>(`/student/fees/${encodeURIComponent(invoice.id)}/razorpay-order`,{method:'POST'},{preserveSessionOnUnauthorized:true})
      await loadRazorpayCheckout()
      if (!window.Razorpay) throw new Error('Razorpay Checkout is unavailable.')
      const checkout=new window.Razorpay({
        key:order.keyId, order_id:order.orderId, amount:order.amount, currency:order.currency,
        name:order.name, description:order.description, prefill:order.prefill,
        theme:{color:'#7065f0'},
        handler:async (result:RazorpayCheckoutResult) => {
          try {
            const receipt=await apiRequest<any>('/student/fees/razorpay/verify',{method:'POST',body:JSON.stringify({razorpayOrderId:result.razorpay_order_id,razorpayPaymentId:result.razorpay_payment_id,razorpaySignature:result.razorpay_signature})},{preserveSessionOnUnauthorized:true})
            const fresh=await apiRequest<any>('/portal/me',undefined,{preserveSessionOnUnauthorized:true}); setPortal(fresh)
            showToast(`Payment verified. Receipt ${receipt.receiptNumber}.`)
          } catch(error) {
            showToast(error instanceof Error?error.message:'Payment verification failed. Contact campus support before retrying.')
          } finally { setPaymentBusy(null) }
        },
        modal:{ondismiss:()=>setPaymentBusy(null)},
      })
      checkout.on('payment.failed',(result:any)=>{
        setPaymentBusy(null)
        showToast(result?.error?.description||'Payment was not completed. You can retry from the fee ledger.')
      })
      checkout.open()
    } catch(error) {
      setPaymentBusy(null)
      showToast(error instanceof Error?error.message:'Could not start Razorpay Checkout.')
    }
  }
  const publishAnnouncement = async () => {
    if(!announcementForm.title.trim()||!announcementForm.content.trim()) { showToast('Add a title and message.'); return }
    try {
      if(USE_API) await apiRequest('/admin/announcements',{method:'POST',body:JSON.stringify({...announcementForm,priority:'NORMAL',pinned:false})})
      if(USE_API) {
        const rows=await apiRequest<any[]>('/announcements')
        setNotices(rows.map(row=>({title:row.title,detail:row.content,time:new Date(row.created_at).toLocaleString(),tone:'purple'})))
      } else setNotices(prev=>[{title:announcementForm.title,detail:announcementForm.content,time:'Just now',tone:'purple'},...prev])
      setDrawer(null);setAnnouncementForm({title:'',content:'',audience:'ALL'});showToast('Announcement published.')
    } catch(error) { showToast(error instanceof Error?error.message:'Could not publish announcement.') }
  }
  const createAccount = async () => {
    if(!accountForm.userId.trim()||!accountForm.fullName.trim()||!accountForm.email.includes('@')||accountForm.initialPassword.length<12) { showToast('Enter an ID, name, valid email, and an initial password of at least 12 characters.'); return }
    try {
      await apiRequest('/admin/users',{method:'POST',body:JSON.stringify(accountForm)})
      const rows=await apiRequest<any[]>('/admin/users');setAccounts(rows);setDrawer(null);setAccountForm({userId:'',fullName:'',email:'',role:'STUDENT',initialPassword:'',department:'Computer Science'});showToast('Account created. It must change the initial password at next sign-in.')
    } catch(error) { showToast(error instanceof Error?error.message:'Could not create account.') }
  }
  const setAccountStatus = async (user:any) => {
    try { await apiRequest(`/admin/users/${user.userId}/status`,{method:'PATCH',body:JSON.stringify({active:!user.active})});setAccounts(prev=>prev.map(row=>row.userId===user.userId?{...row,active:!user.active}:row));showToast('Account status updated.') }
    catch(error) { showToast(error instanceof Error?error.message:'Could not update account.') }
  }
  const requirePasswordChange = async (user:any) => {
    try { await apiRequest(`/admin/users/${user.userId}/force-password-change`,{method:'PATCH'});setAccounts(prev=>prev.map(row=>row.userId===user.userId?{...row,mustChangePassword:true}:row));showToast('Password change required at next sign-in.') }
    catch(error) { showToast(error instanceof Error?error.message:'Could not update account.') }
  }
  const createSupportTicket = async () => {
    if(!supportForm.subject.trim()||!supportForm.description.trim()) { showToast('Enter a subject and describe the issue.'); return }
    try { const result=await apiRequest<any>('/support',{method:'POST',body:JSON.stringify(supportForm)});const rows=await apiRequest<any[]>('/support');setPortal(prev=>({...prev,supportTickets:rows}));setDrawer(null);setSupportForm({category:'ACADEMIC',subject:'',description:'',priority:'MEDIUM'});showToast(`Support ticket ${result.ticketNumber} submitted.`) }
    catch(error) { showToast(error instanceof Error?error.message:'Could not create support ticket.') }
  }
  const refreshStudentCourses = async () => {
    const [data,catalog]=await Promise.all([apiRequest<any>('/portal/me'),apiRequest<any[]>('/student/courses')])
    setPortal(prev=>({...prev,...data}));setAvailableCourses(catalog)
    setCourses((data.courses||[]).map((row:any,index:number)=>({id:row.code,title:row.title,code:row.code,faculty:row.faculty||'',students:0,completion:Number(row.completion||0),color:palette[index%palette.length],moduleCount:Number(row.module_count||0),completedModules:Number(row.completed_modules||0),attendedSessions:Number(row.attended_sessions||0),sessions:Number(row.sessions||0)})))
  }
  const enrollInCourse = async (row:any) => {
    try { await apiRequest('/student/courses/enroll',{method:'POST',body:JSON.stringify({courseCode:row.code,sectionCode:row.section_code})});await refreshStudentCourses();setDrawer(null);showToast('Course registration completed.') }
    catch(error) { showToast(error instanceof Error?error.message:'Course registration failed.') }
  }
  const dropCourse = async (courseCode:string) => {
    try { await apiRequest(`/student/courses/${courseCode}`,{method:'DELETE'});await refreshStudentCourses();setDrawer(null);showToast('Course dropped.') }
    catch(error) { showToast(error instanceof Error?error.message:'Course could not be dropped.') }
  }
  const openNotification = async (notification:any) => {
    try { if(USE_API&&notification.id) { await apiRequest(`/notifications/${notification.id}/read`,{method:'PATCH'});setPortal(prev=>({...prev,notifications:(prev.notifications||[]).map((n:any)=>n.id===notification.id?{...n,read:true}:n)})) } }
    catch(error) { showToast(error instanceof Error?error.message:'Could not update notification.') }
    const link=String(notification.link_url||'').toLowerCase()
    const next=link.includes('fee')?'Fees & payments':link.includes('attendance')?'Attendance':link.includes('assignment')?'Assignments':link.includes('timetable')?'Timetable':link.includes('grade')?'Academic progress':'Announcements'
    setNoticeOpen(false);setPage(next)
  }
  const renderAssignments = () => <section className="panel table-panel"><div className="panel-heading"><div><h3>{role==='Faculty'?'Assignments for my courses':'My assignments'}</h3><p>Assignment records and submission status from the academic database.</p></div>{role==='Faculty'&&<button className="button primary" onClick={()=>{setAssignmentForm({code:courses[0]?.code||'',title:'',description:'',totalMarks:100,weightage:10,dueDate:''});setDrawer({title:'Create assignment',kind:'newAssignment'})}}><Plus size={16}/> Create assignment</button>}</div><div className="table-scroll"><table><thead><tr><th>Course</th><th>Assignment</th><th>Due</th><th>Marks</th><th>Status / pending</th><th></th></tr></thead><tbody>{(portal.assignments||[]).map((a:any)=><tr key={a.id}><td><span className="id-tag">{a.code}</span></td><td><b>{a.title}</b><small className="table-subtext">{a.course}</small></td><td>{new Date(a.due_date).toLocaleString()}</td><td>{a.total_marks}</td><td>{role==='Faculty'?`${a.pending_count||0} pending`:a.submission_status||'Not submitted'}</td><td>{role==='Student'&&<button className="button secondary small" onClick={()=>{setSubmissionText('');setDrawer({title:a.title,kind:'submitAssignment',data:a})}}>{a.submission_status?'Update submission':'Submit'}</button>}</td></tr>)}</tbody></table></div>{!portal.assignments?.length&&<div className="empty-state"><FileText size={28}/><b>No assignments yet</b><span>Published assignment records will show here.</span></div>}</section>
  const renderStudentAttendanceMatrix = () => {
    const records=portal.attendance||[]
    const courses=Array.from(new Map(records.map((record:any)=>[record.code||record.course_code,{code:record.code||record.course_code,title:record.title||record.course_title||record.code}])).values()) as {code:string;title:string}[]
    const days=['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday']
    return <section className="panel table-panel"><div className="panel-heading"><div><h3>My attendance register</h3><p>Weekdays are rows and courses are columns. Each cell lists the recorded class dates and status.</p></div></div>{records.length?<div className="attendance-matrix-scroll"><div className="attendance-matrix" style={{gridTemplateColumns:`minmax(100px,.7fr) repeat(${courses.length},minmax(170px,1fr))`}}><div className="attendance-matrix-head attendance-matrix-day-head">Weekday</div>{courses.map(course=><div className="attendance-matrix-head" key={course.code}><b>{course.code}</b><small>{course.title}</small></div>)}{days.map(day=><Fragment key={day}><div className="attendance-matrix-day">{day}</div>{courses.map(course=>{
      const entries=records.filter((record:any)=>{
        const recordDate=String(record.class_date||'').slice(0,10)
        const weekday=recordDate?new Date(`${recordDate}T00:00:00`).getDay():-1
        return (record.code||record.course_code)===course.code&&days[weekday]===day
      }).sort((a:any,b:any)=>String(b.class_date).localeCompare(String(a.class_date)))
      return <div className="attendance-matrix-cell" key={`${day}-${course.code}`}>{entries.length?entries.map((record:any,index:number)=>{
        const date=String(record.class_date||'').slice(0,10)
        const label=date?new Date(`${date}T00:00:00`).toLocaleDateString(undefined,{month:'short',day:'numeric'}):'Date unavailable'
        const status=String(record.status||'UNKNOWN').toUpperCase()
        const attended=['PRESENT','LATE','EXCUSED'].includes(status)
        return <div className="attendance-matrix-entry" key={record.id||`${date}-${index}`}><span>{label}</span><span className={`status ${attended?'active':'risk'}`}><i/>{status}</span></div>
      }):<span className="attendance-matrix-empty">—</span>}</div>
    })}</Fragment>)}</div></div>:<div className="empty-state"><ClipboardCheck size={28}/><b>No attendance recorded</b><span>Your faculty will publish records after class.</span></div>}</section>
  }
  const renderAttendance = () => role === 'Student' ? renderStudentAttendanceMatrix() : <div className="content-grid two-one"><section className="panel"><div className="panel-heading"><div><h3>Attendance records</h3><p>Records returned by the API for your assigned courses.</p></div><span className="status active">Live data</span></div><div className="table-scroll"><table><thead><tr><th>Date</th><th>Student</th><th>Course</th><th>Status</th></tr></thead><tbody>{(portal.attendance||[]).map((r:any)=><tr key={r.id}><td>{new Date(r.class_date).toLocaleDateString()}</td><td>{r.student_name||r.student_id}</td><td>{r.code}</td><td>{r.status}</td></tr>)}</tbody></table></div></section><section className="panel"><div className="panel-heading"><div><h3>Mark attendance</h3><p>{courses[courseIndex%Math.max(courses.length,1)]?.code||'No course'} · Today</p></div><ClipboardCheck size={20} className="muted-icon"/></div>{courses.length>1&&<label>Course<select value={courseIndex} onChange={e=>setCourseIndex(Number(e.target.value))}>{courses.map((c,i)=><option value={i} key={c.code}>{c.code} · {c.title}</option>)}</select></label>}<p className="helper-text">Choose a status for each student, then save the register.</p><div className="attendance-roster">{students.slice(0,30).map(s=><div className="roster-row" key={s.id}><span className="avatar student-avatar">{s.name.split(' ').map(n=>n[0]).slice(0,2).join('')}</span><span className="roster-name"><b>{s.name}</b><small>{s.id}</small></span><select value={attendance[s.id]||'Present'} onChange={e=>setAttendance(prev=>({...prev,[s.id]:e.target.value}))}><option>Present</option><option>Absent</option><option>Late</option><option>Excused</option></select></div>)}</div><button className="button primary full" onClick={saveAttendance}><Check size={16}/> Save attendance</button></section></div>

  const renderTimetableGrid = (rows:any[]) => {
    const days = ['Monday','Tuesday','Wednesday','Thursday','Friday']
    const minutes = (value:any) => { const [hour,minute] = String(value||'09:00').slice(0,5).split(':').map(Number); return (hour||0)*60+(minute||0) }
    const startHour = Math.min(9,...rows.map(row=>Math.floor(minutes(row.start_time??row.start)/60)))
    const endHour = Math.max(17,...rows.map(row=>Math.ceil(minutes(row.end_time??row.end)/60)))
    const hours = Array.from({length:Math.max(1,endHour-startHour)},(_,index)=>startHour+index)
    const hourLabel = (hour:number) => `${String(hour%12||12).padStart(2,'0')}:00 ${hour<12?'AM':'PM'}`
    const events = rows.flatMap((row:any,index:number)=>{
      const dayValue=String(row.day_of_week??row.day??'').toUpperCase()
      const dayIndex=days.findIndex(day=>day.toUpperCase().startsWith(dayValue.slice(0,3)))
      if(dayIndex<0)return []
      const start=minutes(row.start_time??row.start),end=minutes(row.end_time??row.end)
      const from=Math.max(0,Math.floor(start/60)-startHour),to=Math.min(hours.length,Math.max(from+1,Math.ceil(end/60)-startHour))
      const title=row.title??row.course_title??row.course??'Class'
      const code=row.code??row.course_code
      const room=[row.building,row.room_number].filter(Boolean).join(' · ')||row.room||'Room not assigned'
      return [{row,index,dayIndex,from,to,title,code,room,startLabel:String(row.start_time??row.start??'').slice(0,5),endLabel:String(row.end_time??row.end??'').slice(0,5)}]
    })
    return <div className="weekly-grid-scroll"><div className="weekly-grid" style={{gridTemplateColumns:`minmax(88px,.75fr) repeat(${hours.length},minmax(100px,1fr))`,gridTemplateRows:`48px repeat(${days.length},minmax(94px,auto))`}}>
      <div className="weekly-grid-corner" style={{gridColumn:1,gridRow:1}}>Day</div>
      {hours.map((hour,index)=><div className="weekly-grid-hour" key={hour} style={{gridColumn:index+2,gridRow:1}}>{hourLabel(hour)}</div>)}
      {days.map((day,dayIndex)=><Fragment key={day}><div className="weekly-grid-day" style={{gridColumn:1,gridRow:dayIndex+2}}>{day}</div>{hours.map((hour,index)=><div className="weekly-grid-cell" key={`${day}-${hour}`} style={{gridColumn:index+2,gridRow:dayIndex+2}}/>)}</Fragment>)}
      {events.map(({row,index,dayIndex,from,to,title,code,room,startLabel,endLabel})=><button className={`weekly-grid-class block-${index%4}`} key={`${days[dayIndex]}-${startLabel}-${code||title}-${index}`} style={{gridColumn:`${from+2} / ${to+2}`,gridRow:dayIndex+2}} onClick={()=>setDrawer({title:code?`${code} · ${title}`:title,kind:'schedule',data:{title,code,day:days[dayIndex],time:`${startLabel}–${endLabel}`,faculty:row.faculty,section:row.section_code,room}})}><b>{code?`${code} · `:''}{title}</b><span>{startLabel}–{endLabel}</span><small>{room}</small></button>)}
    </div></div>
  }
  const renderTimetable = () => <section className="panel"><div className="panel-heading"><div><h3>Weekly timetable</h3><p>Hours across the top and weekdays down the side.</p></div><button className="button secondary" onClick={()=>showToast('Timetable export is ready in a future module.')}><Download size={15}/> Export</button></div>{renderTimetableGrid([
    {day:'Monday',start:'09:00',end:'10:00',code:'CS501',title:'Data Structures & Algorithms',faculty:'Dr. Ananya Rao',room:'Room B-204'},
    {day:'Monday',start:'11:00',end:'12:00',code:'AI520',title:'Machine Learning',faculty:'Dr. Sana Iqbal',room:'Room C-110'},
    {day:'Monday',start:'14:00',end:'16:00',code:'DB510',title:'Database Systems',faculty:'Prof. Vikram Das',room:'Lab 3 · Block A'},
    {day:'Tuesday',start:'10:00',end:'11:00',code:'DB510',title:'Database Systems',faculty:'Prof. Vikram Das',room:'Room B-204'},
    {day:'Tuesday',start:'14:00',end:'16:00',code:'AI520',title:'Machine Learning',faculty:'Dr. Sana Iqbal',room:'Innovation Lab'},
    {day:'Wednesday',start:'09:00',end:'10:00',code:'CS501',title:'Data Structures & Algorithms',faculty:'Dr. Ananya Rao',room:'Room B-204'},
    {day:'Wednesday',start:'12:00',end:'13:00',code:'WD505',title:'Full Stack Development',faculty:'Dr. Rajesh Kumar',room:'Lab 2'},
    {day:'Thursday',start:'10:00',end:'11:00',code:'DB510',title:'Database Systems',faculty:'Prof. Vikram Das',room:'Room B-204'},
    {day:'Thursday',start:'11:00',end:'12:00',code:'WD505',title:'Full Stack Development',faculty:'Dr. Rajesh Kumar',room:'Room B-204'},
    {day:'Friday',start:'10:00',end:'11:00',code:'CS501',title:'Data Structures & Algorithms',faculty:'Dr. Ananya Rao',room:'Room B-204'},
    {day:'Friday',start:'14:00',end:'16:00',code:'DB510',title:'Database Systems',faculty:'Prof. Vikram Das',room:'Lab 3 · Block A'},
  ])}</section>

  const renderProgress = () => <><div className="stats-grid three-stats"><div className="stat-card"><div className="stat-icon lavender"><GraduationCap size={21}/></div><div className="stat-label">Current CGPA</div><div className="stat-value">8.72 <small>/ 10</small></div><div className="stat-foot"><span className="positive"><ArrowUpRight size={14}/>0.34</span><span>vs. last semester</span></div></div><div className="stat-card"><div className="stat-icon mint"><BookOpen size={21}/></div><div className="stat-label">Credits completed</div><div className="stat-value">84 <small>/ 120</small></div><div className="mini-progress"><span style={{width:'70%',background:'#28b89b'}}/></div></div><div className="stat-card"><div className="stat-icon peach"><ClipboardCheck size={21}/></div><div className="stat-label">Course completion</div><div className="stat-value">72%</div><div className="mini-progress"><span style={{width:'72%',background:'#f5a544'}}/></div></div></div><div className="content-grid two-one"><section className="panel"><div className="panel-heading"><div><h3>Semester-by-semester CGPA</h3><p>Your academic growth over time.</p></div></div><div className="chart-area tall"><ResponsiveContainer width="100%" height="100%"><AreaChart data={semesterData}><CartesianGrid strokeDasharray="4 5" vertical={false} stroke="var(--line)"/><XAxis dataKey="name" axisLine={false} tickLine={false}/><YAxis domain={[0,10]} axisLine={false} tickLine={false}/><Tooltip/><Area dataKey="cgpa" stroke="#7065f0" fill="#7065f022" strokeWidth={3}/></AreaChart></ResponsiveContainer></div></section><section className="panel"><div className="panel-heading"><div><h3>Degree progress</h3><p>Credits earned to date.</p></div></div><div className="degree-donut"><ResponsiveContainer width="100%" height={190}><PieChart><Pie data={[{name:'Earned',value:84},{name:'Remaining',value:36}]} dataKey="value" cx="50%" cy="50%" innerRadius={59} outerRadius={77} startAngle={90} endAngle={-270} stroke="none"><Cell fill="#7065f0"/><Cell fill="var(--soft)"/></Pie></PieChart></ResponsiveContainer><div className="degree-center"><b>70%</b><span>completed</span></div></div><div className="degree-legend"><span><i className="legend-dot purple"/>84 credits earned</span><span><i className="legend-dot soft"/>36 credits remaining</span></div></section></div></>

  const renderPayments = () => <>
    <div className="stats-grid three-stats">
      <div className="stat-card"><div className="stat-icon lavender"><CircleDollarSign size={21}/></div><div className="stat-label">Total fees</div><div className="stat-value">₹1,20,000</div><div className="stat-foot"><span>Academic year 2026–27</span></div></div>
      <div className="stat-card"><div className="stat-icon mint"><CheckCircle2 size={21}/></div><div className="stat-label">Paid so far</div><div className="stat-value">₹90,000</div><div className="stat-foot"><span className="positive">75% settled</span></div></div>
      <div className="stat-card"><div className="stat-icon peach"><Wallet size={21}/></div><div className="stat-label">Outstanding</div><div className="stat-value">₹30,000</div><div className="stat-foot"><span className="warning-text">Due 30 Oct 2026</span></div></div>
    </div>
    <section className="panel table-panel">
      <div className="panel-heading"><div><h3>Fee ledger</h3><p>Sample invoices · Read-only</p></div></div>
      <div className="table-scroll"><table><thead><tr><th>Invoice</th><th>Description</th><th>Due date</th><th>Amount</th><th>Status</th><th></th></tr></thead><tbody>
        {[{id:'INV-2601',desc:'Tuition fee · Semester 5',due:'10 Aug 2026',amount:'₹60,000',status:'Paid'},{id:'INV-2602',desc:'Examination fee',due:'15 Sep 2026',amount:'₹10,000',status:'Paid'},{id:'INV-2603',desc:'Tuition fee · Semester 5 (part 2)',due:'30 Oct 2026',amount:'₹30,000',status:'Pending'},{id:'INV-2604',desc:'Library and lab fee',due:'30 Oct 2026',amount:'₹20,000',status:'Paid'}].map(r=><tr key={r.id}><td><span className="id-tag">{r.id}</span></td><td>{r.desc}</td><td>{r.due}</td><td><b>{r.amount}</b></td><td><span className={`status ${r.status==='Paid'?'active':'pending'}`}><i/>{r.status}</span></td><td><button className="icon-button" onClick={()=>setDrawer({title:r.id,kind:'invoice',data:r})}><MoreHorizontal size={18}/></button></td></tr>)}
      </tbody></table></div>
      <p className="helper-text">These sample rows cannot be paid. For Razorpay test checkout, sign in as a student and pay an invoice from the API fee ledger.</p>
    </section>
  </>

  const renderAnnouncements = () => <section className="panel"><div className="panel-heading"><div><h3>Campus announcements</h3><p>Updates and important dates for your community.</p></div>{role==='Admin'&&<button className="button primary" onClick={()=>setDrawer({title:'Create announcement',kind:'newNotice'})}><Plus size={16}/> New announcement</button>}</div><div className="announcement-list">{notices.map(n=><div className="announcement-row" key={n.title}><span className={`notice-symbol ${n.tone}`}><Bell size={17}/></span><div className="announcement-text"><b>{n.title}</b><p>{n.detail}</p><small>{n.time}</small></div><button className="icon-button" onClick={()=>setDrawer({title:n.title,kind:'notice',data:n})}><ChevronRight size={18}/></button></div>)}</div></section>

  const renderSupport = () => <section className="panel table-panel"><div className="panel-heading"><div><h3>Support requests</h3><p>{role==='Admin'?'Campus tickets submitted by portal users.':'Track the status of your support requests.'}</p></div>{role!=='Admin'&&<button className="button primary" onClick={()=>setDrawer({title:'New support request',kind:'newSupport'})}><Plus size={16}/> New request</button>}</div><div className="table-scroll"><table><thead><tr><th>Ticket</th>{role==='Admin'&&<th>Submitted by</th>}<th>Subject</th><th>Category</th><th>Priority</th><th>Status</th><th>Created</th></tr></thead><tbody>{(portal.supportTickets||[]).map((r:any)=><tr key={r.ticket_number}><td><span className="id-tag">{r.ticket_number}</span></td>{role==='Admin'&&<td>{r.user_id}</td>}<td>{r.subject}</td><td>{r.category}</td><td>{r.priority}</td><td>{r.status}</td><td>{new Date(r.created_at).toLocaleDateString()}</td></tr>)}</tbody></table></div>{!portal.supportTickets?.length&&<div className="empty-state"><CircleHelp size={28}/><b>No support requests</b><span>New requests will appear here.</span></div>}</section>

  const renderProfile = () => <section className="panel profile-form-panel"><div className="panel-heading"><div><h3>My profile</h3><p>Official identity fields are managed by campus administrators.</p></div></div><div className="profile-readonly"><div className="detail-line"><span>Name</span><b>{portal.profile?.name}</b></div><div className="detail-line"><span>Institutional ID</span><b>{portal.profile?.id}</b></div><div className="detail-line"><span>Email</span><b>{portal.profile?.email}</b></div><div className="detail-line"><span>Department</span><b>{portal.profile?.department}</b></div></div><form className="profile-edit-grid" onSubmit={async e=>{e.preventDefault();const body=role==='Student'?{phone:(e.currentTarget.elements.namedItem('phone') as HTMLInputElement).value,hobbies:(e.currentTarget.elements.namedItem('hobbies') as HTMLInputElement).value,extracurriculars:(e.currentTarget.elements.namedItem('extracurriculars') as HTMLInputElement).value}:{phone:(e.currentTarget.elements.namedItem('phone') as HTMLInputElement).value,officeRoom:(e.currentTarget.elements.namedItem('officeRoom') as HTMLInputElement).value,qualification:(e.currentTarget.elements.namedItem('qualification') as HTMLInputElement).value,specialization:(e.currentTarget.elements.namedItem('specialization') as HTMLInputElement).value};try{const saved=await apiRequest<any>(role==='Student'?'/student/profile':'/faculty/profile',{method:'PUT',body:JSON.stringify(body)});setPortal(prev=>({...prev,profile:{...prev.profile,...saved}}));showToast('Profile saved.')}catch(error){showToast(error instanceof Error?error.message:'Profile could not be saved.')}}}><label>Phone<input name="phone" defaultValue={portal.profile?.phone||''}/></label>{role==='Student'?<><label>Hobbies and interests<input name="hobbies" defaultValue={portal.profile?.hobbies||''}/></label><label>Extracurricular activities<textarea name="extracurriculars" rows={3} defaultValue={portal.profile?.extracurriculars||''}/></label></>:<><label>Office location<input name="officeRoom" defaultValue={portal.profile?.office_room||''}/></label><label>Qualifications<input name="qualification" defaultValue={portal.profile?.qualification||''}/></label><label>Specialization<input name="specialization" defaultValue={portal.profile?.specialization||''}/></label></>}<button className="button primary" type="submit"><Check size={16}/> Save profile</button></form></section>

  const renderGeneric = () => <section className="panel generic-panel"><div className="generic-hero"><div className="stat-icon lavender"><Sparkles size={24}/></div><h3>{page}</h3><p>This workspace is ready to connect to the AcadVision API and academic workflows.</p></div><div className="generic-cards">{[{icon:BookOpen,title:'Academic records',text:'View relevant course and semester information.'},{icon:ClipboardCheck,title:'Pending actions',text:'Review tasks and complete academic workflows.'},{icon:ShieldCheck,title:'Secure access',text:'Actions are intended to follow role-based permissions.'}].map(x=><div className="generic-card" key={x.title}><x.icon size={22}/><b>{x.title}</b><span>{x.text}</span></div>)}</div><button className="button primary" onClick={()=>setDrawer({title:`New ${page.toLowerCase()} item`,kind:'genericCreate'})}><Plus size={16}/> Add record</button></section>

  const renderAdminAttendance = () => <section className="panel table-panel"><div className="panel-heading"><div><h3>Campus attendance register</h3><p>Recent records from all courses.</p></div><span className="status active">API data</span></div><div className="table-scroll"><table><thead><tr><th>Date</th><th>Student</th><th>Course</th><th>Status</th><th>Marked by</th></tr></thead><tbody>{(portal.attendance||[]).map((r:any)=><tr key={r.id}><td>{new Date(r.class_date).toLocaleDateString()}</td><td>{r.student_id}</td><td>{r.course_code}</td><td>{r.status}</td><td>{r.marked_by}</td></tr>)}</tbody></table></div></section>

  const renderLiveTimetable = () => <section className="panel"><div className="panel-heading"><div><h3>My weekly timetable</h3><p>Hours across the top and weekdays down the side.</p></div></div>{portal.timetable?.length?renderTimetableGrid(portal.timetable):<div className="empty-state"><CalendarDays size={28}/><b>No classes scheduled</b><span>Your timetable will appear here when sections are assigned.</span></div>}</section>

  const renderLiveProgress = () => { const earned=Number(portal.profile?.credits||0), required=Number(portal.profile?.credits_required||120), pct=required?Math.round(earned/required*100):0; return <><div className="stats-grid three-stats"><div className="stat-card"><div className="stat-icon lavender"><GraduationCap size={21}/></div><div className="stat-label">Current CGPA</div><div className="stat-value">{Number(portal.profile?.cgpa||0).toFixed(2)} <small>/ 10</small></div></div><div className="stat-card"><div className="stat-icon mint"><BookOpen size={21}/></div><div className="stat-label">Credits completed</div><div className="stat-value">{earned} <small>/ {required}</small></div><div className="mini-progress"><span style={{width:`${Math.min(pct,100)}%`,background:'#28b89b'}}/></div></div><div className="stat-card"><div className="stat-icon peach"><ClipboardCheck size={21}/></div><div className="stat-label">Average course completion</div><div className="stat-value">{courses.length?Math.round(courses.reduce((s,c)=>s+c.completion,0)/courses.length):0}%</div></div></div><div className="content-grid two-one"><section className="panel"><div className="panel-heading"><div><h3>CGPA by semester</h3><p>Published institutional GPA records.</p></div></div><div className="chart-area tall"><ResponsiveContainer width="100%" height="100%"><AreaChart data={portal.gpaHistory||[]}><CartesianGrid strokeDasharray="4 5" vertical={false} stroke="var(--line)"/><XAxis dataKey="semester_number" name="Semester"/><YAxis domain={[0,10]}/><Tooltip/><Area dataKey="cumulative_cgpa" name="CGPA" stroke="#7065f0" fill="#7065f022" strokeWidth={3}/></AreaChart></ResponsiveContainer></div>{!portal.gpaHistory?.length&&<p className="helper-text">No published semester GPA records.</p>}</section><section className="panel"><div className="panel-heading"><div><h3>Course progress</h3><p>Completion is based on completed course modules.</p></div></div>{courses.map(c=><div className="detail-line" key={c.code}><span>{c.code}</span><b>{c.completedModules||0}/{c.moduleCount||0} modules · {c.completion}%</b></div>)}</section></div></> }

  const renderLiveFees = () => {
    const fees=portal.fees||[]
    const total=fees.reduce((s:number,r:any)=>s+Number(r.net_payable??r.amount??0),0)
    const outstanding=fees.filter((r:any)=>r.status!=='PAID').reduce((s:number,r:any)=>s+Number(r.net_payable??r.amount??0),0)
    const money=(n:number)=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:0}).format(n)
    return <><div className="stats-grid three-stats"><div className="stat-card"><div className="stat-label">Total billed</div><div className="stat-value">{money(total)}</div></div><div className="stat-card"><div className="stat-label">Paid</div><div className="stat-value">{money(total-outstanding)}</div></div><div className="stat-card"><div className="stat-label">Outstanding</div><div className="stat-value">{money(outstanding)}</div></div></div><section className="panel table-panel"><div className="panel-heading"><div><h3>Fee ledger</h3><p>Invoices and verified Razorpay payments.</p></div></div><div className="table-scroll"><table><thead><tr><th>Invoice</th><th>Description</th><th>Type</th><th>Due date</th><th>Amount</th><th>Status</th><th>Payment</th></tr></thead><tbody>{fees.map((r:any)=><tr key={r.id}><td><span className="id-tag">{r.id}</span></td><td>{r.description}</td><td>{r.fee_type}</td><td>{new Date(r.due_date).toLocaleDateString()}</td><td><b>{money(Number(r.net_payable??r.amount))}</b></td><td><span className={`status ${r.status==='PAID'?'active':'pending'}`}><i/>{r.status}</span></td><td>{r.status!=='PAID'&&<button className="button primary small" disabled={!!paymentBusy} onClick={()=>payInvoice(r)}><Wallet size={14}/>{paymentBusy===r.id?'Opening…':'Pay with Razorpay'}</button>}</td></tr>)}</tbody></table></div>{!fees.length&&<div className="empty-state"><Wallet size={28}/><b>No fee invoices</b><span>There are no fee records for your account.</span></div>}<p className="helper-text">Checkout uses Razorpay test mode. Configure backend test keys to open checkout; test transactions do not collect real money.</p></section></>
  }

  const content = () => {
    if (page==='Overview') return renderOverview()
    if (page==='Students' && role==='Admin') return renderStudents()
    if (page==='My students' && role==='Faculty') return renderMyStudents()
    if (page==='Access & roles' && role==='Admin' && USE_API) return renderAccounts()
    if (page==='Faculty' && role==='Admin') return renderFaculty()
    if (page==='Courses' || page==='My courses') return renderCourses()
    if (page==='Course registration'&&role==='Student'&&USE_API) return renderRegistration()
    if (page==='Attendance') return USE_API&&role==='Admin'?renderAdminAttendance():renderAttendance()
    if (page==='Timetable') return USE_API&&role!=='Admin'?renderLiveTimetable():renderTimetable()
    if (page==='Academic progress') return USE_API&&role==='Student'?renderLiveProgress():renderProgress()
    if (page==='Fees & payments') return USE_API&&role==='Student'?renderLiveFees():renderPayments()
    if (page==='Assignments') return USE_API ? renderAssignments() : renderGeneric()
    if (page==='Announcements') return renderAnnouncements()
    if (page==='Support'&&USE_API) return renderSupport()
    if (page==='My profile'&&USE_API&&role!=='Admin') return renderProfile()
    return renderGeneric()
  }

  const renderDrawer = () => {
    if (!drawer) return null
    const d = drawer.data
    return <div className="drawer-scrim" onMouseDown={e=>{if(e.target===e.currentTarget)setDrawer(null)}}><aside className="drawer"><div className="drawer-head"><div><span className="eyebrow">ACADVISION WORKSPACE</span><h2>{drawer.title}</h2></div><button className="icon-button close-button" onClick={()=>setDrawer(null)}><X size={20}/></button></div>
      {drawer.kind==='newStudent'||drawer.kind==='editStudent' ? <div className="drawer-body"><p className="drawer-intro">Add academic details and contact information. Required fields are marked with *.</p><label>Student ID *<input value={studentForm.id} disabled={drawer.kind==='editStudent'} onChange={e=>setStudentForm({...studentForm,id:e.target.value})} placeholder="e.g. AV24006"/></label><label>Full name *<input value={studentForm.name} onChange={e=>setStudentForm({...studentForm,name:e.target.value})} placeholder="Student name"/></label><label>Email address *<input type="email" value={studentForm.email} onChange={e=>setStudentForm({...studentForm,email:e.target.value})} placeholder="name@campus.edu"/></label>{USE_API&&drawer.kind==='newStudent'&&<label>Temporary initial password *<input type="password" autoComplete="new-password" minLength={12} value={provisionPassword} onChange={e=>setProvisionPassword(e.target.value)} placeholder="At least 12 characters"/><small className="helper-text">The student must change this password at first sign-in.</small></label>}<label>Department<select value={studentForm.department} onChange={e=>setStudentForm({...studentForm,department:e.target.value})}><option>Computer Science</option><option>Information Technology</option><option>Artificial Intelligence</option><option>Data Science</option><option>Mathematics</option></select></label><div className="form-row"><label>Semester<input type="number" min="1" max="12" value={studentForm.semester} onChange={e=>setStudentForm({...studentForm,semester:Number(e.target.value)})}/></label><label>CGPA<input type="number" min="0" max="10" step=".01" value={studentForm.cgpa} onChange={e=>setStudentForm({...studentForm,cgpa:Number(e.target.value)})}/></label></div><div className="form-row"><label>Attendance %<input type="number" min="0" max="100" value={studentForm.attendance} onChange={e=>setStudentForm({...studentForm,attendance:Number(e.target.value)})}/></label><label>Status<select value={studentForm.status} onChange={e=>setStudentForm({...studentForm,status:e.target.value})}><option>Active</option><option>At risk</option><option>Inactive</option></select></label></div><label>Hobbies and interests<input value={studentForm.hobbies} onChange={e=>setStudentForm({...studentForm,hobbies:e.target.value})} placeholder="e.g. robotics, music"/></label><div className="drawer-actions"><button className="button secondary" onClick={()=>setDrawer(null)}>Cancel</button><button className="button primary" onClick={drawer.kind==='newStudent'?addStudent:saveEditedStudent}><Check size={16}/> Save student</button></div></div>
      : drawer.kind==='newFaculty'||drawer.kind==='editFaculty' ? <div className="drawer-body"><p className="drawer-intro">Create a faculty profile and login account. Required fields are marked with *.</p><label>Faculty ID *<input value={facultyForm.id} disabled={drawer.kind==='editFaculty'} onChange={e=>setFacultyForm({...facultyForm,id:e.target.value})} placeholder="e.g. FAC1004"/></label><label>Full name *<input value={facultyForm.name} onChange={e=>setFacultyForm({...facultyForm,name:e.target.value})} placeholder="Faculty name"/></label><label>Email address *<input type="email" value={facultyForm.email} onChange={e=>setFacultyForm({...facultyForm,email:e.target.value})} placeholder="name@campus.edu"/></label>{USE_API&&drawer.kind==='newFaculty'&&<label>Temporary initial password *<input type="password" autoComplete="new-password" minLength={12} value={provisionPassword} onChange={e=>setProvisionPassword(e.target.value)} placeholder="At least 12 characters"/><small className="helper-text">The faculty member must change this password at first sign-in.</small></label>}<label>Department<select value={facultyForm.department} onChange={e=>setFacultyForm({...facultyForm,department:e.target.value})}><option>Computer Science</option><option>Information Technology</option><option>Artificial Intelligence</option><option>Data Science</option><option>Mathematics</option></select></label><label>Assigned course count<input type="number" min="0" value={facultyForm.courses} onChange={e=>setFacultyForm({...facultyForm,courses:Number(e.target.value)})}/></label><div className="drawer-actions"><button className="button secondary" onClick={()=>setDrawer(null)}>Cancel</button><button className="button primary" onClick={drawer.kind==='newFaculty'?addFaculty:saveEditedFaculty}><Check size={16}/> Save faculty</button></div></div>
      : drawer.kind==='studentActions' ? <div className="drawer-body"><div className="profile-hero"><span className="avatar student-avatar xl">{d.name.split(' ').map((n:string)=>n[0]).slice(0,2).join('')}</span><div><h3>{d.name}</h3><span className="id-tag">{d.id}</span></div></div><div className="detail-stats"><div><span>CGPA</span><b>{d.cgpa.toFixed(2)}</b></div><div><span>Attendance</span><b>{d.attendance}%</b></div><div><span>Semester</span><b>{d.semester}</b></div></div><div className="action-list"><button onClick={()=>openStudentForm(d)}><FileText size={17}/> Edit student record <ChevronRight size={16}/></button><button onClick={()=>{setDrawer({title:`${d.name} · dashboard preview`,kind:'studentDetail',data:d});showToast('Read-only student dashboard preview opened.')}}><PanelRight size={17}/> Preview student dashboard <ChevronRight size={16}/></button><button onClick={()=>{setStudents(prev=>prev.map(s=>s.id===d.id?{...s,status:s.status==='Inactive'?'Active':'Inactive'}:s));setDrawer(null);showToast('Student status updated.')}}><ShieldCheck size={17}/> {d.status==='Inactive'?'Reactivate account':'Deactivate account'} <ChevronRight size={16}/></button><button className="danger-action" onClick={()=>{if(window.confirm(`Remove ${d.name} from active demo records?`))deleteStudent(d.id);setDrawer(null)}}><X size={17}/> Remove demo record <ChevronRight size={16}/></button></div><p className="helper-text">Production systems should archive academic records and audit all account changes rather than permanently deleting them.</p></div>
      : drawer.kind==='facultyActions' ? <div className="drawer-body"><div className="profile-hero"><span className="avatar faculty-avatar xl">{d.name.replace('Dr. ','').replace('Prof. ','').split(' ').map((n:string)=>n[0]).slice(0,2).join('')}</span><div><h3>{d.name}</h3><span className="id-tag">{d.id}</span></div></div><div className="detail-stats"><div><span>Courses</span><b>{d.courses}</b></div><div><span>Status</span><b>{d.status}</b></div><div><span>Department</span><b>{d.department.split(' ')[0]}</b></div></div><div className="action-list"><button onClick={()=>openFacultyForm(d)}><FileText size={17}/> Edit faculty record <ChevronRight size={16}/></button><button onClick={()=>{setDrawer({title:`${d.name} · dashboard preview`,kind:'facultyDetail',data:d});showToast('Read-only faculty dashboard preview opened.')}}><PanelRight size={17}/> Preview faculty dashboard <ChevronRight size={16}/></button><button onClick={()=>{setFaculty(prev=>prev.map(s=>s.id===d.id?{...s,status:s.status==='Inactive'?'Active':'Inactive'}:s));setDrawer(null);showToast('Faculty status updated.')}}><ShieldCheck size={17}/> {d.status==='Inactive'?'Reactivate account':'Deactivate account'} <ChevronRight size={16}/></button><button onClick={()=>showToast('Password reset workflow is represented in the backend/API setup guide.')}><ShieldCheck size={17}/> Initiate secure password reset <ChevronRight size={16}/></button></div></div>
      : drawer.kind==='studentDetail'||drawer.kind==='facultyDetail' ? <div className="drawer-body"><div className="profile-hero"><span className={`avatar xl ${drawer.kind==='studentDetail'?'student-avatar':'faculty-avatar'}`}>{d.name.split(' ').map((n:string)=>n[0]).slice(0,2).join('')}</span><div><h3>{d.name}</h3><p>{d.email}</p><span className="id-tag">{d.id}</span></div></div><div className="drawer-section"><h4>Profile details</h4><div className="detail-line"><span>Department</span><b>{d.department}</b></div><div className="detail-line"><span>Account status</span><b>{d.status}</b></div>{drawer.kind==='studentDetail'?<><div className="detail-line"><span>Current semester</span><b>{d.semester}</b></div><div className="detail-line"><span>CGPA</span><b>{d.cgpa.toFixed(2)} / 10</b></div><div className="detail-line"><span>Attendance</span><b>{d.attendance}%</b></div><div className="detail-line"><span>Hobbies</span><b>{d.hobbies||'Not provided'}</b></div></>:<div className="detail-line"><span>Assigned courses</span><b>{d.courses}</b></div>}</div><div className="drawer-section"><h4>Academic snapshot</h4><div className="snapshot-bar"><span style={{width:`${drawer.kind==='studentDetail'?d.attendance:Math.min(d.courses*20,100)}%`}}/></div><p className="helper-text">{drawer.kind==='studentDetail'?'Attendance snapshot. Select the relevant workspace for course-wise history.':'Teaching workload snapshot. Course-level details are available in My courses.'}</p></div><button className="button secondary full" onClick={()=>{setDrawer(null);setPage(drawer.kind==='studentDetail'?'Students':'Faculty')}}>Return to directory</button></div>
      : drawer.kind==='course' ? <div className="drawer-body"><div className="course-detail-head"><span className="course-symbol large" style={{background:`${d.color}18`,color:d.color}}><BookOpen size={24}/></span><div><span className="id-tag">{d.code}</span><h3>{d.title}</h3><p>{d.faculty}</p></div></div><div className="detail-stats"><div><span>Completion</span><b>{d.completion}%</b></div><div><span>Students</span><b>{d.students}</b></div><div><span>Pending</span><b>{100-d.completion}%</b></div></div><div className="drawer-section"><h4>Course progress</h4><div className="snapshot-bar"><span style={{width:`${d.completion}%`,background:d.color}}/></div><div className="detail-line"><span>Modules completed</span><b>{Math.round(d.completion/10)} / 10</b></div><div className="detail-line"><span>Average attendance</span><b>89%</b></div><div className="detail-line"><span>Assessment average</span><b>82%</b></div></div><div className="drawer-section"><h4>Students needing attention</h4>{students.filter(s=>s.attendance<75).map(s=><div className="notice-mini" key={s.id}><AlertCircle size={16}/><span>{s.name} · Attendance {s.attendance}%</span></div>)}{students.every(s=>s.attendance>=75)&&<p className="helper-text">No attendance alerts in the demo roster.</p>}</div><button className="button primary full" onClick={()=>{setDrawer(null);setPage('Attendance')}}>Open attendance workspace <ChevronRight size={16}/></button></div>
      : drawer.kind==='newAssignment' ? <div className="drawer-body"><label>Course<select value={assignmentForm.code} onChange={e=>setAssignmentForm({...assignmentForm,code:e.target.value})}>{courses.map(c=><option key={c.code} value={c.code}>{c.code} · {c.title}</option>)}</select></label><label>Title<input value={assignmentForm.title} onChange={e=>setAssignmentForm({...assignmentForm,title:e.target.value})} required/></label><label>Description<textarea rows={4} value={assignmentForm.description} onChange={e=>setAssignmentForm({...assignmentForm,description:e.target.value})}/></label><div className="form-grid"><label>Maximum marks<input type="number" min="1" value={assignmentForm.totalMarks} onChange={e=>setAssignmentForm({...assignmentForm,totalMarks:Number(e.target.value)})}/></label><label>Weight (%)<input type="number" min="1" max="100" value={assignmentForm.weightage} onChange={e=>setAssignmentForm({...assignmentForm,weightage:Number(e.target.value)})}/></label></div><label>Due date<input type="datetime-local" value={assignmentForm.dueDate} onChange={e=>setAssignmentForm({...assignmentForm,dueDate:e.target.value})}/></label><div className="drawer-actions"><button className="button secondary" onClick={()=>setDrawer(null)}>Cancel</button><button className="button primary" onClick={createAssignment}><Check size={16}/> Publish assignment</button></div></div>
      : drawer.kind==='submitAssignment' ? <div className="drawer-body"><p className="drawer-intro">{d.course} · Due {new Date(d.due_date).toLocaleString()}</p><label>Submission text<textarea rows={10} value={submissionText} onChange={e=>setSubmissionText(e.target.value)} placeholder="Write your response or include a secure document link."/></label><p className="helper-text">This submission form stores text only. Secure file upload will be enabled after storage and malware-scanning are configured.</p><div className="drawer-actions"><button className="button secondary" onClick={()=>setDrawer(null)}>Cancel</button><button className="button primary" onClick={submitAssignment}><Check size={16}/> Submit work</button></div></div>
      : drawer.kind==='newAccount' ? <div className="drawer-body"><p className="drawer-intro">The account is linked to a student or faculty profile. The user must change this initial password at first sign-in.</p><label>User ID *<input value={accountForm.userId} onChange={e=>setAccountForm({...accountForm,userId:e.target.value})}/></label><label>Full name *<input value={accountForm.fullName} onChange={e=>setAccountForm({...accountForm,fullName:e.target.value})}/></label><label>Email *<input type="email" value={accountForm.email} onChange={e=>setAccountForm({...accountForm,email:e.target.value})}/></label><label>Role<select value={accountForm.role} onChange={e=>setAccountForm({...accountForm,role:e.target.value})}><option value="STUDENT">Student</option><option value="FACULTY">Faculty</option></select></label><label>Initial password *<input type="password" minLength={12} value={accountForm.initialPassword} onChange={e=>setAccountForm({...accountForm,initialPassword:e.target.value})}/></label><p className="helper-text">Use a temporary password and deliver it through your institution’s approved secure channel.</p><div className="drawer-actions"><button className="button secondary" onClick={()=>setDrawer(null)}>Cancel</button><button className="button primary" onClick={createAccount}><Check size={16}/> Create account</button></div></div>
      : drawer.kind==='newSupport' ? <div className="drawer-body"><label>Category<select value={supportForm.category} onChange={e=>setSupportForm({...supportForm,category:e.target.value})}><option value="ACADEMIC">Academic</option><option value="ATTENDANCE">Attendance</option><option value="FEE_PAYMENT">Fee payment</option><option value="TIMETABLE">Timetable</option><option value="TECHNICAL">Technical</option></select></label><label>Priority<select value={supportForm.priority} onChange={e=>setSupportForm({...supportForm,priority:e.target.value})}><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option></select></label><label>Subject<input value={supportForm.subject} onChange={e=>setSupportForm({...supportForm,subject:e.target.value})}/></label><label>Details<textarea rows={6} value={supportForm.description} onChange={e=>setSupportForm({...supportForm,description:e.target.value})}/></label><div className="drawer-actions"><button className="button secondary" onClick={()=>setDrawer(null)}>Cancel</button><button className="button primary" onClick={createSupportTicket}><Check size={16}/> Submit request</button></div></div>
      : drawer.kind==='newNotice' ? <div className="drawer-body"><label>Announcement title<input value={announcementForm.title} onChange={e=>setAnnouncementForm({...announcementForm,title:e.target.value})} placeholder="e.g. Registration deadline"/></label><label>Message<textarea rows={4} value={announcementForm.content} onChange={e=>setAnnouncementForm({...announcementForm,content:e.target.value})} placeholder="Write a clear announcement for your campus..."/></label><label>Audience<select value={announcementForm.audience} onChange={e=>setAnnouncementForm({...announcementForm,audience:e.target.value})}><option value="ALL">All users</option><option value="STUDENTS">Students</option><option value="FACULTY">Faculty</option></select></label><div className="drawer-actions"><button className="button secondary" onClick={()=>setDrawer(null)}>Cancel</button><button className="button primary" onClick={publishAnnouncement}><Check size={16}/> Publish</button></div></div>
      : drawer.kind==='invoice' ? <div className="drawer-body"><div className="payment-callout"><FileText size={24}/><span><b>{d.id}</b><strong>{d.amount}</strong><small>{d.desc}</small></span></div><div className="detail-line"><span>Due date</span><b>{d.due}</b></div><div className="detail-line"><span>Status</span><b>{d.status}</b></div><button className="button secondary full" onClick={()=>showToast('Receipt/invoice download is a placeholder in demo mode.')}>Download invoice</button></div>
      : <div className="drawer-body"><div className="generic-hero"><Sparkles size={28}/><h3>{d?.title||page}</h3><p>{d?.detail||'This feature can be extended through the REST API.'}</p></div><button className="button primary full" onClick={()=>{setDrawer(null);showToast('Workspace opened.')}}>Done</button></div>}
    </aside></div>
  }

  return <div className={`app-shell ${dark?'dark':''}`}>
    <aside className={`sidebar ${sidebarOpen?'':'collapsed'}`}>
      <div className="brand-row"><div className="brand-mark"><Activity size={22}/></div>{sidebarOpen&&<div className="brand-name">Acad<span>Vision</span><small>CAMPUS PORTAL</small></div>}<button className="sidebar-toggle" onClick={()=>setSidebarOpen(!sidebarOpen)}><Menu size={18}/></button></div>
      <div className="role-label">{sidebarOpen?'WORKSPACE':''}</div>
      <div className="role-switcher"><div className="role-avatar"><ShieldCheck size={16}/></div>{sidebarOpen&&<><div className="role-switch-copy"><b>{role} workspace</b><span>{USE_API?'Institutional account':'Switch dashboard'}</span></div>{!USE_API&&<ChevronDown size={15}/>}</>}{!USE_API&&<select aria-label="Switch role" value={role} onChange={e=>changeRole(e.target.value as Role)}><option>Admin</option><option>Faculty</option><option>Student</option></select>}</div>
      <nav className="side-nav">{nav.map(group=><div className="nav-group" key={group.label}><span className="nav-group-label">{sidebarOpen?group.label.toUpperCase():''}</span>{group.items.map(([label,Icon]:any)=><button key={label} className={`nav-item ${page===label?'active':''}`} onClick={()=>{setPage(label);setQuery('');setDrawer(null);setSidebarOpen(window.innerWidth<760?false:sidebarOpen)}} title={label}><Icon size={18}/>{sidebarOpen&&<span>{label}</span>}{sidebarOpen&&label==='Announcements'&&<span className="nav-badge">{notices.length}</span>}</button>)}</div>)}</nav>
      <div className="sidebar-bottom"><div className="help-card"><div className="help-icon"><CircleHelp size={17}/></div>{sidebarOpen&&<div><b>Need a hand?</b><span>Visit help center</span></div>}</div><button className="nav-item" onClick={()=>onLogout?onLogout():showToast('You are viewing a local demo. No session was logged out.')}><LogOut size={18}/>{sidebarOpen&&<span>Log out</span>}</button><div className="sidebar-user"><span className="avatar admin-avatar">AV</span>{sidebarOpen&&<div><b>{role==='Admin'?'Alex Morgan':role==='Faculty'?'Dr. Ananya Rao':'Aarav Sharma'}</b><span>{role==='Admin'?'Campus administrator':role==='Faculty'?'Faculty member':'Student account'}</span></div>}<button className="icon-button" onClick={()=>setPage(role==='Admin'?'Settings':'My profile')}><MoreHorizontal size={18}/></button></div></div>
    </aside>
    <main className="main-area">
      <header className="topbar"><div className="breadcrumb"><span>Workspace</span><ChevronRight size={14}/><b>{page}</b></div><div className="topbar-actions"><div className="global-search"><Search size={17}/><input placeholder="Search anything..." value={query} onChange={e=>setQuery(e.target.value)} onKeyDown={e=>{if(e.key==='Enter'&&query.trim())showToast(`Searching this workspace for “${query}”`)}}/><kbd>⌘ K</kbd></div><button className="icon-button theme-toggle" onClick={()=>setDark(!dark)} title="Toggle theme">{dark?<Sun size={19}/>:<Moon size={19}/>}</button><button className="icon-button notification-button" onClick={()=>setNoticeOpen(!noticeOpen)} title="Notifications"><Bell size={19}/><i/></button><div className="profile-menu-wrap" ref={profileMenuRef} onMouseEnter={()=>setProfileMenuOpen(true)} onMouseLeave={()=>setProfileMenuOpen(false)}><button className="topbar-avatar" type="button" aria-label="Open profile menu" aria-haspopup="menu" aria-expanded={profileMenuOpen} onClick={()=>setProfileMenuOpen(true)}>{avatarInitials}</button>{profileMenuOpen&&<div className="profile-popover" role="menu"><div className="profile-popover-heading"><span className={`avatar ${role==='Faculty'?'faculty-avatar':role==='Student'?'student-avatar':'admin-avatar'}`}>{avatarInitials}</span><div><b>{account.name}</b><small>{role} account</small></div></div><div className="profile-popover-details"><div><span>ID</span><b>{account.id}</b></div><div><span>Name</span><b>{account.name}</b></div><div><span>Department</span><b>{account.department}</b></div></div><button className="profile-logout-button" type="button" role="menuitem" onClick={()=>{setProfileMenuOpen(false);if(onLogout)void onLogout();else showToast('You are viewing a local demo. No session was logged out.')}}><LogOut size={16}/> Log out</button></div>}</div></div></header>
      {noticeOpen&&<div className="notification-popover"><div className="popover-head"><b>Notifications</b><button onClick={()=>setNoticeOpen(false)}><X size={16}/></button></div>{(USE_API?(portal.notifications||[]):notices).slice(0,5).map((n:any)=><button className="popover-notice" key={n.id||n.title} onClick={()=>openNotification(n)}><span className={`notice-symbol ${n.read?'green':'purple'}`}><Bell size={14}/></span><span><b>{n.title}</b><small>{new Date(n.created_at||Date.now()).toLocaleString()}</small></span></button>)}{!(USE_API?(portal.notifications||[]):notices).length&&<p className="helper-text">You’re all caught up.</p>}<button className="text-action" onClick={()=>{setNoticeOpen(false);setPage('Announcements')}}>View all updates <ChevronRight size={14}/></button></div>}
      <div className="page-content"><div className="page-title-row"><div><div className="page-kicker">{role.toUpperCase()} PORTAL <span>•</span> 2026–27</div><h1>{page}</h1><p>{page==='Overview'?'A bright start to a productive day.':page==='Students'?'All your student records, organized in one place.':page==='Faculty'?'Meet the people inspiring the next generation.':`Your ${page.toLowerCase()} workspace at a glance.`}</p></div><div className="page-title-actions"><span className="date-chip"><CalendarDays size={15}/> Monday, 12 October</span>{role==='Admin'&&page==='Overview'&&<button className="button primary" onClick={()=>openStudentForm()}><Plus size={16}/> Add student</button>}</div></div>
        {content()}
        <footer className="footer"><span>© 2026 AcadVision · Academic management, made clearer.</span><span><i className="online-dot"></i> All systems operational <span className="footer-sep">·</span> Demo workspace</span></footer>
      </div>
    </main>
    {renderDrawer()}
    {toast&&<div className="toast"><CheckCircle2 size={18}/>{toast}<button onClick={()=>setToast('')}><X size={15}/></button></div>}
  </div>
}
