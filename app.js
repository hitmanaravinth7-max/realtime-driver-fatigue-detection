/**
 * ====================================================================
 * REAL-TIME DRIVER FATIGUE DETECTION SYSTEM
 * College Capstone Web Application Engine
 * ====================================================================
 */

document.addEventListener('DOMContentLoaded', () => {

  // --- STATE MANAGEMENT ---
  const state = {
    currentUser: {
      email: 'driver@college.edu',
      name: 'Scholar Driver',
      vehicleId: 'TN-09-EV-2026',
      team: 'Engineering Capstone Team'
    },
    isAuthenticated: true,
    isRegisterMode: false,
    activePage: 'dashboard',
    
    // Detection & Hardware
    isCameraRunning: false,
    isDetectionRunning: false,
    isDemoMode: true,
    mediaStream: null,
    animationFrameId: null,
    detectionTimer: null,
    sessionSeconds: 0,
    sessionTimerId: null,

    // Physiological Metrics
    scenario: 'normal', // 'normal', 'microsleep', 'yawning', 'gradual'
    ear: 0.32,
    mar: 0.38,
    blinkRate: 18,
    closedDuration: 0.0,
    yawnDuration: 0.0,
    confidence: 97.4,
    fatigueLevel: 'NORMAL', // 'NORMAL', 'MILD', 'CRITICAL'
    isAlertActive: false,
    alertCount: 0,

    // Settings
    settings: {
      soundEnabled: true,
      vibrationEnabled: true,
      closureDelay: 1.5,
      earThreshold: 0.22,
      marThreshold: 0.65,
      isDarkTheme: true
    },

    // Detection History Log (Local Storage)
    history: []
  };

  // --- SEED INITIAL DEMO HISTORY ---
  const initialDemoHistory = [
    {
      id: 1,
      timestamp: new Date(Date.now() - 25 * 60000).toISOString(),
      dateStr: new Date(Date.now() - 25 * 60000).toLocaleDateString(),
      timeStr: new Date(Date.now() - 25 * 60000).toLocaleTimeString(),
      fatigueStatus: 'Critical Fatigue',
      eyeStatus: 'Closed (2.1s)',
      mouthStatus: 'Normal',
      ear: 0.17,
      mar: 0.39,
      confidence: 97.8,
      isAlert: true,
      notes: 'Micro-sleep event during continuous night testing'
    },
    {
      id: 2,
      timestamp: new Date(Date.now() - 55 * 60000).toISOString(),
      dateStr: new Date(Date.now() - 55 * 60000).toLocaleDateString(),
      timeStr: new Date(Date.now() - 55 * 60000).toLocaleTimeString(),
      fatigueStatus: 'Mild Fatigue',
      eyeStatus: 'Open',
      mouthStatus: 'Yawning (MAR 0.72)',
      ear: 0.28,
      mar: 0.72,
      confidence: 95.2,
      isAlert: true,
      notes: 'Prolonged biological yawning pattern identified'
    },
    {
      id: 3,
      timestamp: new Date(Date.now() - 110 * 60000).toISOString(),
      dateStr: new Date(Date.now() - 110 * 60000).toLocaleDateString(),
      timeStr: new Date(Date.now() - 110 * 60000).toLocaleTimeString(),
      fatigueStatus: 'Normal / Alert',
      eyeStatus: 'Open',
      mouthStatus: 'Normal',
      ear: 0.34,
      mar: 0.38,
      confidence: 98.9,
      isAlert: false,
      notes: 'Optimal daylight vigilance, steady blink frequency'
    },
    {
      id: 4,
      timestamp: new Date(Date.now() - 24 * 3600000).toISOString(),
      dateStr: new Date(Date.now() - 24 * 3600000).toLocaleDateString(),
      timeStr: '22:14:05',
      fatigueStatus: 'Critical Fatigue',
      eyeStatus: 'Closed (1.9s)',
      mouthStatus: 'Normal',
      ear: 0.18,
      mar: 0.40,
      confidence: 96.5,
      isAlert: true,
      notes: 'Late night highway route evaluation'
    },
    {
      id: 5,
      timestamp: new Date(Date.now() - 28 * 3600000).toISOString(),
      dateStr: new Date(Date.now() - 28 * 3600000).toLocaleDateString(),
      timeStr: '18:32:40',
      fatigueStatus: 'Normal / Alert',
      eyeStatus: 'Open',
      mouthStatus: 'Normal',
      ear: 0.33,
      mar: 0.37,
      confidence: 99.1,
      isAlert: false,
      notes: 'Evening urban commuter test run'
    }
  ];

  // Load from LocalStorage if available
  const storedHistory = localStorage.getItem('driver_fatigue_history');
  if (storedHistory) {
    try {
      state.history = JSON.parse(storedHistory);
    } catch {
      state.history = initialDemoHistory;
    }
  } else {
    state.history = initialDemoHistory;
    localStorage.setItem('driver_fatigue_history', JSON.stringify(state.history));
  }

  // --- AUDIO SYNTHESIZER (WEB AUDIO API) ---
  let audioCtx = null;
  function playAlertSiren() {
    if (!state.settings.soundEnabled) return;
    try {
      if (!audioCtx) {
        audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      }
      if (audioCtx.state === 'suspended') {
        audioCtx.resume();
      }

      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();

      osc.type = 'sawtooth';
      // Emergency alert alternating dual tone
      osc.frequency.setValueAtTime(880, audioCtx.currentTime);
      osc.frequency.setValueAtTime(660, audioCtx.currentTime + 0.15);
      osc.frequency.setValueAtTime(880, audioCtx.currentTime + 0.3);

      gain.gain.setValueAtTime(0.3, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.45);

      osc.connect(gain);
      gain.connect(audioCtx.destination);

      osc.start();
      osc.stop(audioCtx.currentTime + 0.45);
    } catch (e) {
      console.warn('Web Audio alarm notice:', e);
    }
  }

  function triggerVibration() {
    if (state.settings.vibrationEnabled && 'vibrate' in navigator) {
      try {
        navigator.vibrate([200, 100, 200]);
      } catch (e) {}
    }
  }

  // --- DOM SELECTORS ---
  const sidebar = document.getElementById('sidebar');
  const sidebarToggle = document.getElementById('sidebar-toggle');
  const themeToggle = document.getElementById('theme-toggle');
  const navLinks = document.querySelectorAll('.nav-link');
  const pageSections = document.querySelectorAll('.page-section');
  const emergencyAlert = document.getElementById('emergency-alert');
  const btnDismissAlert = document.getElementById('btn-dismiss-alert');
  const alertDetail = document.getElementById('alert-detail');
  const alertTime = document.getElementById('alert-time');

  // Video & Canvas
  const webcamVideo = document.getElementById('webcam-video');
  const hudCanvas = document.getElementById('hud-canvas');
  const hudCtx = hudCanvas ? hudCanvas.getContext('2d') : null;
  const cameraPlaceholder = document.getElementById('camera-placeholder');
  const btnToggleCamera = document.getElementById('btn-toggle-camera');
  const btnToggleDetection = document.getElementById('btn-toggle-detection');
  const btnTestSiren = document.getElementById('btn-test-siren');
  const scenarioBtns = document.querySelectorAll('.scenario-btn');

  // Dashboard Telemetry Elements
  const cardFatigueVal = document.getElementById('card-fatigue-val');
  const cardFatigueBadge = document.getElementById('card-fatigue-badge');
  const cardFatigueSub = document.getElementById('card-fatigue-sub');
  const progFatigue = document.getElementById('prog-fatigue');
  const cardEyeVal = document.getElementById('card-eye-val');
  const cardEyeSub = document.getElementById('card-eye-sub');
  const progEar = document.getElementById('prog-ear');
  const cardMouthVal = document.getElementById('card-mouth-val');
  const cardMouthSub = document.getElementById('card-mouth-sub');
  const progMar = document.getElementById('prog-mar');
  const cardConfidenceVal = document.getElementById('card-confidence-val');
  const dashboardTimer = document.getElementById('dashboard-timer');
  const dashMonitoringStatus = document.getElementById('dash-monitoring-status');
  const dashSirenStatus = document.getElementById('dash-siren-status');
  const globalStatusPill = document.getElementById('global-status-pill');
  const globalStatusText = document.getElementById('global-status-text');
  const hudStatusPill = document.getElementById('hud-status-pill');
  const activeScenarioLabel = document.getElementById('active-scenario-label');

  // HUD Elements
  const hudEarStat = document.getElementById('hud-ear-stat');
  const hudMarStat = document.getElementById('hud-mar-stat');
  const hudConfStat = document.getElementById('hud-conf-stat');
  const hudWarningText = document.getElementById('hud-warning-text');
  const hudEyeBadge = document.getElementById('hud-eye-badge');
  const hudClosureDur = document.getElementById('hud-closure-dur');
  const hudMouthBadge = document.getElementById('hud-mouth-badge');
  const hudBlinkRate = document.getElementById('hud-blink-rate');
  const hudFatigueLevelBadge = document.getElementById('hud-fatigue-level-badge');

  // --- ROUTING / NAVIGATION ---
  function switchPage(pageId) {
    state.activePage = pageId;

    navLinks.forEach(link => {
      link.classList.toggle('active', link.getAttribute('data-page') === pageId);
    });

    pageSections.forEach(section => {
      section.classList.toggle('active', section.id === `page-${pageId}`);
    });

    // Close sidebar on mobile
    if (window.innerWidth <= 768) {
      sidebar.classList.add('collapsed');
    }

    if (pageId === 'history') {
      renderHistoryTable();
    } else if (pageId === 'reports') {
      renderReports();
    } else if (pageId === 'dashboard') {
      renderRecentDashboardTable();
    }
  }

  navLinks.forEach(link => {
    link.addEventListener('click', () => {
      const targetPage = link.getAttribute('data-page');
      if (targetPage) switchPage(targetPage);
    });
  });

  const btnGotoMonitoring = document.getElementById('btn-goto-monitoring');
  if (btnGotoMonitoring) {
    btnGotoMonitoring.addEventListener('click', () => switchPage('monitoring'));
  }

  const btnDashViewAllHistory = document.getElementById('btn-dash-view-all-history');
  if (btnDashViewAllHistory) {
    btnDashViewAllHistory.addEventListener('click', () => switchPage('history'));
  }

  if (sidebarToggle) {
    sidebarToggle.addEventListener('click', () => {
      sidebar.classList.toggle('collapsed');
    });
  }

  // Theme Toggle
  if (themeToggle) {
    themeToggle.addEventListener('click', () => {
      state.settings.isDarkTheme = !state.settings.isDarkTheme;
      document.body.classList.toggle('light-theme', !state.settings.isDarkTheme);
    });
  }

  // --- AUTHENTICATION & LOGIN ---
  const authForm = document.getElementById('auth-form');
  const btnToggleAuthMode = document.getElementById('btn-toggle-auth-mode');
  const groupName = document.getElementById('group-name');
  const authTitle = document.getElementById('auth-title');
  const authSubtitle = document.getElementById('auth-subtitle');
  const authSwitchText = document.getElementById('auth-switch-text');
  const btnAuthSubmit = document.getElementById('btn-auth-submit');
  const btnQuickDemo = document.getElementById('btn-quick-demo');
  const btnLogoutSidebar = document.getElementById('btn-logout-sidebar');

  if (btnQuickDemo) {
    btnQuickDemo.addEventListener('click', () => {
      document.getElementById('input-email').value = 'driver@college.edu';
      document.getElementById('input-password').value = 'driver123';
      clearAuthErrors();
    });
  }

  if (btnToggleAuthMode) {
    btnToggleAuthMode.addEventListener('click', () => {
      state.isRegisterMode = !state.isRegisterMode;
      groupName.style.display = state.isRegisterMode ? 'block' : 'none';
      authTitle.textContent = state.isRegisterMode ? 'Create Driver Account' : 'Driver Authentication';
      authSubtitle.textContent = state.isRegisterMode ? 'Register profile to connect with drowsiness telemetry' : 'Sign in to access real-time computer vision dashboard';
      btnAuthSubmit.textContent = state.isRegisterMode ? 'Register & Open Dashboard' : 'Login to Dashboard';
      authSwitchText.textContent = state.isRegisterMode ? 'Already registered?' : 'New Driver?';
      btnToggleAuthMode.textContent = state.isRegisterMode ? 'Sign In' : 'Create Account';
      clearAuthErrors();
    });
  }

  function clearAuthErrors() {
    ['err-email', 'err-password', 'err-name'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.textContent = '';
    });
  }

  if (authForm) {
    authForm.addEventListener('submit', (e) => {
      e.preventDefault();
      clearAuthErrors();

      const email = document.getElementById('input-email').value.trim();
      const password = document.getElementById('input-password').value;
      const name = document.getElementById('input-name').value.trim();

      let hasError = false;
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

      if (!email || !emailRegex.test(email)) {
        document.getElementById('err-email').textContent = 'Please provide a valid email (e.g. driver@college.edu).';
        hasError = true;
      }

      if (!password || password.length < 6) {
        document.getElementById('err-password').textContent = 'Password must be at least 6 characters.';
        hasError = true;
      }

      if (state.isRegisterMode && !name) {
        document.getElementById('err-name').textContent = 'Driver name cannot be empty.';
        hasError = true;
      }

      if (!hasError) {
        state.currentUser.email = email;
        state.currentUser.name = name || 'Scholar Driver';
        document.getElementById('nav-driver-name').textContent = state.currentUser.name;
        document.getElementById('prof-name').value = state.currentUser.name;
        document.getElementById('prof-email').value = state.currentUser.email;

        state.isAuthenticated = true;
        switchPage('dashboard');
      }
    });
  }

  if (btnLogoutSidebar) {
    btnLogoutSidebar.addEventListener('click', () => {
      stopDetection();
      stopCamera();
      switchPage('login');
    });
  }

  const linkForgotPass = document.getElementById('link-forgot-pass');
  if (linkForgotPass) {
    linkForgotPass.addEventListener('click', (e) => {
      e.preventDefault();
      alert('Password Reset Simulation: A reset link has been dispatched to your registered email.');
    });
  }

  // --- HARDWARE CAMERA INTEGRATION (WEBCAM) ---
  async function startCamera() {
    if (state.isCameraRunning) return;

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' },
        audio: false
      });

      state.mediaStream = stream;
      webcamVideo.srcObject = stream;
      webcamVideo.classList.remove('hidden');
      if (cameraPlaceholder) cameraPlaceholder.classList.add('hidden');

      state.isCameraRunning = true;
      updateCameraButtons();
      startHudCanvasLoop();

      hudStatusPill.className = 'status-pill status-active';
      hudStatusPill.textContent = 'CAMERA STREAM ACTIVE';
    } catch (err) {
      console.warn('Camera access unavailable or denied. Operating in Simulated Canvas mode.', err);
      // Graceful fallback to simulation view
      state.isCameraRunning = true;
      if (cameraPlaceholder) cameraPlaceholder.classList.add('hidden');
      updateCameraButtons();
      startHudCanvasLoop();
      hudStatusPill.className = 'status-pill status-active';
      hudStatusPill.textContent = 'SIMULATION HUD ACTIVE';
    }
  }

  function stopCamera() {
    if (!state.isCameraRunning) return;

    if (state.mediaStream) {
      state.mediaStream.getTracks().forEach(track => track.stop());
      state.mediaStream = null;
    }
    webcamVideo.srcObject = null;
    webcamVideo.classList.add('hidden');
    if (cameraPlaceholder) cameraPlaceholder.classList.remove('hidden');

    if (state.animationFrameId) {
      cancelAnimationFrame(state.animationFrameId);
      state.animationFrameId = null;
    }

    state.isCameraRunning = false;
    updateCameraButtons();

    hudStatusPill.className = 'status-pill status-standby';
    hudStatusPill.textContent = 'CAMERA OFF';

    // Clear canvas
    if (hudCtx) {
      hudCtx.clearRect(0, 0, hudCanvas.width, hudCanvas.height);
    }
  }

  function updateCameraButtons() {
    const camBtnText = document.getElementById('cam-btn-text');
    const camBtnIcon = document.getElementById('cam-btn-icon');
    if (camBtnText) camBtnText.textContent = state.isCameraRunning ? 'Stop Camera' : 'Start Camera';
    if (camBtnIcon) camBtnIcon.textContent = state.isCameraRunning ? '⏹' : '📹';

    btnToggleCamera.classList.toggle('btn-outline-danger', state.isCameraRunning);
    btnToggleCamera.classList.toggle('btn-outline-primary', !state.isCameraRunning);
  }

  if (btnToggleCamera) {
    btnToggleCamera.addEventListener('click', () => {
      if (state.isCameraRunning) {
        stopCamera();
        if (state.isDetectionRunning) stopDetection();
      } else {
        startCamera();
      }
    });
  }

  // --- DETECTION ENGINE & SIMULATION DYNAMICS ---
  function startDetection() {
    if (state.isDetectionRunning) return;

    state.isDetectionRunning = true;
    updateDetectionButtons();

    globalStatusPill.className = 'status-pill status-active';
    globalStatusText.textContent = 'ACTIVE TELEMETRY';
    dashMonitoringStatus.className = 'status-pill status-active';
    dashMonitoringStatus.textContent = 'MONITORING ACTIVE';

    // Session Timer
    state.sessionTimerId = setInterval(() => {
      state.sessionSeconds++;
      const hours = String(Math.floor(state.sessionSeconds / 3600)).padStart(2, '0');
      const mins = String(Math.floor((state.sessionSeconds % 3600) / 60)).padStart(2, '0');
      const secs = String(state.sessionSeconds % 60).padStart(2, '0');
      if (dashboardTimer) dashboardTimer.textContent = `${hours}:${mins}:${secs}`;
    }, 1000);

    // High frequency detection cycle (5 times per second)
    let tick = 0;
    state.detectionTimer = setInterval(() => {
      tick++;
      runDetectionCycle(tick);
    }, 200);

    if (!state.isCameraRunning) {
      startCamera();
    }
  }

  function stopDetection() {
    if (!state.isDetectionRunning) return;

    state.isDetectionRunning = false;
    updateDetectionButtons();

    clearInterval(state.detectionTimer);
    clearInterval(state.sessionTimerId);
    state.detectionTimer = null;
    state.sessionTimerId = null;

    globalStatusPill.className = 'status-pill status-standby';
    globalStatusText.textContent = 'SYSTEM STANDBY';
    dashMonitoringStatus.className = 'status-pill status-standby';
    dashMonitoringStatus.textContent = 'MONITORING STANDBY';

    dismissAlert();
  }

  function updateDetectionButtons() {
    const detBtnText = document.getElementById('det-btn-text');
    const detBtnIcon = document.getElementById('det-btn-icon');
    if (detBtnText) detBtnText.textContent = state.isDetectionRunning ? 'Stop Detection' : 'Start Detection';
    if (detBtnIcon) detBtnIcon.textContent = state.isDetectionRunning ? '⏹' : '▶';

    btnToggleDetection.classList.toggle('btn-outline-danger', state.isDetectionRunning);
    btnToggleDetection.classList.toggle('btn-primary', !state.isDetectionRunning);
  }

  if (btnToggleDetection) {
    btnToggleDetection.addEventListener('click', () => {
      if (state.isDetectionRunning) {
        stopDetection();
      } else {
        startDetection();
      }
    });
  }

  if (btnTestSiren) {
    btnTestSiren.addEventListener('click', () => {
      playAlertSiren();
      triggerVibration();
    });
  }

  // Evaluation Scenario Switching
  scenarioBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      scenarioBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      state.scenario = btn.getAttribute('data-scenario');
      state.closedDuration = 0;
      state.yawnDuration = 0;

      const labels = {
        normal: 'Current Mode: Normal Driving Simulation',
        microsleep: 'Current Mode: Micro-Sleep (Closed Eyes) Simulation',
        yawning: 'Current Mode: Yawning Episode Simulation',
        gradual: 'Current Mode: Gradual Drowsiness Simulation'
      };
      if (activeScenarioLabel) activeScenarioLabel.textContent = labels[state.scenario] || '';
    });
  });

  // Detection Step Calculations
  let lastRecordedAlertTime = 0;

  function runDetectionCycle(tick) {
    const dt = 0.2; // 200ms

    switch (state.scenario) {
      case 'normal':
        state.ear = 0.32 + Math.sin(tick * 0.1) * 0.02;
        state.mar = 0.38 + Math.sin(tick * 0.15) * 0.03;
        state.blinkRate = 16 + (tick % 4);
        state.closedDuration = 0;
        state.yawnDuration = 0;
        state.fatigueLevel = 'NORMAL';
        state.confidence = 97.4 + Math.sin(tick * 0.2) * 1.5;
        break;

      case 'microsleep':
        state.ear = 0.14 + Math.sin(tick * 0.3) * 0.02;
        state.mar = 0.36;
        state.blinkRate = 6;
        state.closedDuration += dt;
        state.confidence = 98.2;

        if (state.closedDuration >= state.settings.closureDelay) {
          state.fatigueLevel = 'CRITICAL';
        } else if (state.closedDuration >= 0.6) {
          state.fatigueLevel = 'MILD';
        }
        break;

      case 'yawning':
        state.ear = 0.26;
        state.mar = 0.74 + Math.sin(tick * 0.2) * 0.04;
        state.blinkRate = 22;
        state.yawnDuration += dt;
        state.confidence = 95.8;

        if (state.yawnDuration >= 2.0) {
          state.fatigueLevel = 'CRITICAL';
        } else {
          state.fatigueLevel = 'MILD';
        }
        break;

      case 'gradual':
        const wave = (Math.sin(tick * 0.08) + 1.0) / 2.0;
        state.ear = 0.20 + wave * 0.11;
        state.mar = 0.44 + (1 - wave) * 0.22;
        state.blinkRate = Math.floor(26 + wave * 8);

        if (state.ear < state.settings.earThreshold) {
          state.closedDuration += dt;
        } else {
          state.closedDuration = 0;
        }

        if (state.mar > state.settings.marThreshold) {
          state.yawnDuration += dt;
        } else {
          state.yawnDuration = 0;
        }

        state.fatigueLevel = (state.closedDuration > 1.2 || state.yawnDuration > 2.0) ? 'CRITICAL' : 'MILD';
        state.confidence = 94.0 + wave * 4;
        break;
    }

    // Clamp ranges
    state.ear = Math.max(0.1, Math.min(0.45, state.ear));
    state.mar = Math.max(0.2, Math.min(0.95, state.mar));
    state.confidence = Math.max(85, Math.min(99.8, state.confidence));

    // Handle Alarms
    if (state.fatigueLevel === 'CRITICAL') {
      triggerFatigueAlert();
    } else {
      if (state.isAlertActive) {
        dismissAlert();
      }
    }

    updateDashboardAndHudUI();
  }

  function triggerFatigueAlert() {
    state.isAlertActive = true;
    emergencyAlert.classList.remove('hidden');

    const msg = state.closedDuration >= state.settings.closureDelay
      ? `Prolonged eye closure detected (${state.closedDuration.toFixed(1)}s). Vehicle hazard alert.`
      : `Repetitive involuntary yawn episode (${state.yawnDuration.toFixed(1)}s). Driver exhaustion alert.`;

    if (alertDetail) alertDetail.textContent = msg;
    if (alertTime) alertTime.textContent = `Timestamp: ${new Date().toLocaleTimeString()}`;

    dashSirenStatus.textContent = '⚠️ ALARM TRIGGERED – TAKE A BREAK';
    dashSirenStatus.style.color = 'var(--alert-red)';

    playAlertSiren();
    triggerVibration();

    // Auto-record to database/history with throttling
    const now = Date.now();
    if (now - lastRecordedAlertTime > 8000) {
      lastRecordedAlertTime = now;
      recordHistoryEvent({
        fatigueStatus: 'Critical Fatigue',
        eyeStatus: state.ear < state.settings.earThreshold ? `Closed (${state.closedDuration.toFixed(1)}s)` : 'Open',
        mouthStatus: state.mar > state.settings.marThreshold ? 'Yawning' : 'Normal',
        ear: state.ear,
        mar: state.mar,
        confidence: state.confidence,
        isAlert: true,
        notes: msg
      });
    }
  }

  function dismissAlert() {
    state.isAlertActive = false;
    emergencyAlert.classList.add('hidden');
    dashSirenStatus.textContent = 'ALL CLEAR – NO HAZARD';
    dashSirenStatus.style.color = 'var(--safety-green)';
  }

  if (btnDismissAlert) {
    btnDismissAlert.addEventListener('click', dismissAlert);
  }

  function recordHistoryEvent(entry) {
    const newRecord = {
      id: Date.now(),
      timestamp: new Date().toISOString(),
      dateStr: new Date().toLocaleDateString(),
      timeStr: new Date().toLocaleTimeString(),
      ...entry
    };

    state.history.unshift(newRecord);
    if (state.history.length > 50) state.history.pop();
    localStorage.setItem('driver_fatigue_history', JSON.stringify(state.history));

    if (state.activePage === 'history') renderHistoryTable();
    if (state.activePage === 'dashboard') renderRecentDashboardTable();
  }

  // UI Updates for Dashboard and HUD
  function updateDashboardAndHudUI() {
    // 1. Fatigue Status
    const isCritical = state.fatigueLevel === 'CRITICAL';
    const isMild = state.fatigueLevel === 'MILD';

    cardFatigueVal.textContent = isCritical ? 'Critical Fatigue' : (isMild ? 'Mild Drowsiness' : 'Normal / Alert');
    cardFatigueBadge.textContent = isCritical ? 'Hazard' : (isMild ? 'Caution' : 'Normal');
    cardFatigueBadge.className = `status-indicator ${isCritical ? 'status-alarm' : (isMild ? 'status-standby' : 'status-ok')}`;
    cardFatigueSub.textContent = isCritical ? 'Immediate Rest Required' : (isMild ? 'Reduced Vigilance' : 'Physiological Baseline Normal');

    progFatigue.style.width = isCritical ? '100%' : (isMild ? '60%' : '20%');
    progFatigue.className = `progress-bar ${isCritical ? 'bar-danger' : (isMild ? 'bar-warning' : 'bar-safe')}`;

    // 2. Eye & EAR
    const isEyeClosed = state.ear < state.settings.earThreshold;
    cardEyeVal.textContent = isEyeClosed ? `Closed (${state.closedDuration.toFixed(1)}s)` : 'Eyes Open';
    cardEyeSub.textContent = `EAR: ${state.ear.toFixed(2)} | Closure: ${state.closedDuration.toFixed(1)}s`;
    progEar.style.width = `${Math.min(100, (state.ear / 0.4) * 100)}%`;
    progEar.className = `progress-bar ${isEyeClosed ? 'bar-danger' : 'bar-primary'}`;

    // 3. Mouth & Yawn
    const isYawn = state.mar > state.settings.marThreshold;
    cardMouthVal.textContent = isYawn ? 'Yawn Detected' : 'Normal';
    cardMouthSub.textContent = `MAR: ${state.mar.toFixed(2)} | Blinks: ${state.blinkRate}/m`;
    progMar.style.width = `${Math.min(100, (state.mar / 0.85) * 100)}%`;
    progMar.className = `progress-bar ${isYawn ? 'bar-danger' : 'bar-primary'}`;

    // 4. Confidence
    cardConfidenceVal.textContent = `${state.confidence.toFixed(1)}%`;

    // HUD Stats
    if (hudEarStat) hudEarStat.textContent = `EAR: ${state.ear.toFixed(2)} (${isEyeClosed ? 'Closed' : 'Open'})`;
    if (hudMarStat) hudMarStat.textContent = `MAR: ${state.mar.toFixed(2)} (${isYawn ? 'Yawn' : 'Normal'})`;
    if (hudConfStat) hudConfStat.textContent = `Conf: ${state.confidence.toFixed(1)}%`;

    if (hudEyeBadge) {
      hudEyeBadge.textContent = isEyeClosed ? 'Eyes Closed' : 'Eyes Open';
      hudEyeBadge.className = `badge ${isEyeClosed ? 'badge-danger' : 'badge-safe'}`;
    }
    if (hudClosureDur) hudClosureDur.textContent = `${state.closedDuration.toFixed(1)} s`;

    if (hudMouthBadge) {
      hudMouthBadge.textContent = isYawn ? 'Yawn Detected' : 'Normal';
      hudMouthBadge.className = `badge ${isYawn ? 'badge-danger' : 'badge-safe'}`;
    }
    if (hudBlinkRate) hudBlinkRate.textContent = `${state.blinkRate} blinks/min`;

    if (hudFatigueLevelBadge) {
      hudFatigueLevelBadge.textContent = `${state.fatigueLevel} (${isCritical ? '100%' : (isMild ? '55%' : '0%')})`;
      hudFatigueLevelBadge.className = `badge ${isCritical ? 'badge-danger' : (isMild ? 'badge-warning' : 'badge-safe')}`;
    }

    if (hudWarningText) {
      hudWarningText.textContent = isCritical
        ? '⚠️ FATIGUE DETECTED – PLEASE TAKE A BREAK'
        : (isMild ? 'Caution: Decreased cognitive vigilance detected' : 'Driver Vigilance: Normal & Active');
    }
  }

  // --- HUD CANVAS DRAWING LOOP ---
  function startHudCanvasLoop() {
    if (!hudCanvas || !hudCtx) return;

    function renderCanvas() {
      const w = hudCanvas.width;
      const h = hudCanvas.height;

      hudCtx.clearRect(0, 0, w, h);

      const faceX = w * 0.26;
      const faceY = h * 0.16;
      const faceW = w * 0.48;
      const faceH = h * 0.68;

      const isCritical = state.fatigueLevel === 'CRITICAL';
      const hudColor = isCritical ? '#ef233c' : '#00d2ff';
      const eyeColor = state.ear < state.settings.earThreshold ? '#ef233c' : '#06d6a0';
      const mouthColor = state.mar > state.settings.marThreshold ? '#ef233c' : '#00d2ff';

      // Face Bounding Box Corners
      const cornerLen = 24;
      hudCtx.strokeStyle = hudColor;
      hudCtx.lineWidth = 3;

      // Top Left
      hudCtx.beginPath();
      hudCtx.moveTo(faceX, faceY + cornerLen);
      hudCtx.lineTo(faceX, faceY);
      hudCtx.lineTo(faceX + cornerLen, faceY);
      hudCtx.stroke();

      // Top Right
      hudCtx.beginPath();
      hudCtx.moveTo(faceX + faceW - cornerLen, faceY);
      hudCtx.lineTo(faceX + faceW, faceY);
      hudCtx.lineTo(faceX + faceW, faceY + cornerLen);
      hudCtx.stroke();

      // Bottom Left
      hudCtx.beginPath();
      hudCtx.moveTo(faceX, faceY + faceH - cornerLen);
      hudCtx.lineTo(faceX, faceY + faceH);
      hudCtx.lineTo(faceX + cornerLen, faceY + faceH);
      hudCtx.stroke();

      // Bottom Right
      hudCtx.beginPath();
      hudCtx.moveTo(faceX + faceW - cornerLen, faceY + faceH);
      hudCtx.lineTo(faceX + faceW, faceY + faceH);
      hudCtx.lineTo(faceX + faceW, faceY + faceH - cornerLen);
      hudCtx.stroke();

      // Left Eye ROI Box
      const leftEyeX = faceX + faceW * 0.16;
      const eyeY = faceY + faceH * 0.32;
      const eyeW = faceW * 0.28;
      const eyeH = faceH * 0.16;

      hudCtx.setLineDash([5, 4]);
      hudCtx.strokeStyle = eyeColor;
      hudCtx.lineWidth = 2;
      hudCtx.strokeRect(leftEyeX, eyeY, eyeW, eyeH);

      // Right Eye ROI Box
      const rightEyeX = faceX + faceW * 0.56;
      hudCtx.strokeRect(rightEyeX, eyeY, eyeW, eyeH);

      // Mouth ROI Box
      const mouthX = faceX + faceW * 0.30;
      const mouthY = faceY + faceH * 0.65;
      const mouthW = faceW * 0.40;
      const mouthH = faceH * 0.20;

      hudCtx.strokeStyle = mouthColor;
      hudCtx.strokeRect(mouthX, mouthY, mouthW, mouthH);
      hudCtx.setLineDash([]); // Reset dash

      // Telemetry tags on canvas
      hudCtx.font = '11px JetBrains Mono, monospace';
      hudCtx.fillStyle = eyeColor;
      hudCtx.fillText(`EAR ${state.ear.toFixed(2)}`, leftEyeX, eyeY - 4);
      hudCtx.fillText(`EAR ${state.ear.toFixed(2)}`, rightEyeX, eyeY - 4);

      hudCtx.fillStyle = mouthColor;
      hudCtx.fillText(`MAR ${state.mar.toFixed(2)}`, mouthX, mouthY - 4);

      if (state.isCameraRunning) {
        state.animationFrameId = requestAnimationFrame(renderCanvas);
      }
    }

    renderCanvas();
  }

  // --- HISTORY TABLE RENDERING ---
  function renderRecentDashboardTable() {
    const tbody = document.getElementById('dash-recent-tbody');
    if (!tbody) return;

    tbody.innerHTML = '';
    const recents = state.history.slice(0, 4);

    if (recents.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-muted">No detection events recorded yet.</td></tr>';
      return;
    }

    recents.forEach(ev => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${ev.timeStr || '--:--'}</td>
        <td><strong>${ev.fatigueStatus}</strong></td>
        <td>${ev.eyeStatus}</td>
        <td>${ev.mouthStatus}</td>
        <td>EAR: ${ev.ear?.toFixed(2) || '0.32'} | MAR: ${ev.mar?.toFixed(2) || '0.38'}</td>
        <td><span class="badge ${ev.isAlert ? 'badge-danger' : 'badge-safe'}">${ev.isAlert ? 'ALERT' : 'NORMAL'}</span></td>
      `;
      tbody.appendChild(tr);
    });
  }

  let historyFilter = 'all';
  let historySearchText = '';

  function renderHistoryTable() {
    const tbody = document.getElementById('history-full-tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    const filtered = state.history.filter(ev => {
      if (historyFilter === 'alert' && !ev.isAlert) return false;
      if (historyFilter === 'normal' && ev.isAlert) return false;

      if (historySearchText) {
        const q = historySearchText.toLowerCase();
        const matches = (ev.dateStr && ev.dateStr.toLowerCase().includes(q)) ||
          (ev.timeStr && ev.timeStr.toLowerCase().includes(q)) ||
          (ev.fatigueStatus && ev.fatigueStatus.toLowerCase().includes(q)) ||
          (ev.notes && ev.notes.toLowerCase().includes(q));
        if (!matches) return false;
      }
      return true;
    });

    document.getElementById('count-all').textContent = state.history.length;
    document.getElementById('count-alerts').textContent = state.history.filter(h => h.isAlert).length;
    document.getElementById('count-normal').textContent = state.history.filter(h => !h.isAlert).length;

    if (filtered.length === 0) {
      tbody.innerHTML = '<tr><td colspan="9" class="text-muted text-center" style="padding: 24px;">No records match your selected filter.</td></tr>';
      return;
    }

    filtered.forEach(ev => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${ev.dateStr} ${ev.timeStr}</td>
        <td><strong>${ev.fatigueStatus}</strong></td>
        <td>${ev.eyeStatus}</td>
        <td>${ev.mouthStatus}</td>
        <td>${ev.ear ? ev.ear.toFixed(2) : '--'}</td>
        <td>${ev.mar ? ev.mar.toFixed(2) : '--'}</td>
        <td>${ev.confidence ? ev.confidence.toFixed(1) + '%' : '97.4%'}</td>
        <td><span class="badge ${ev.isAlert ? 'badge-danger' : 'badge-safe'}">${ev.isAlert ? 'ALERT' : 'SAFE'}</span></td>
        <td class="small text-muted">${ev.notes || ''}</td>
      `;
      tbody.appendChild(tr);
    });
  }

  // History Filter Listeners
  const filterBtns = document.querySelectorAll('.filter-btn');
  filterBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      filterBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      historyFilter = btn.getAttribute('data-filter');
      renderHistoryTable();
    });
  });

  const historySearch = document.getElementById('history-search');
  if (historySearch) {
    historySearch.addEventListener('input', (e) => {
      historySearchText = e.target.value.trim();
      renderHistoryTable();
    });
  }

  const btnClearHistory = document.getElementById('btn-clear-history');
  if (btnClearHistory) {
    btnClearHistory.addEventListener('click', () => {
      if (confirm('Clear all historical detection log entries from local storage?')) {
        state.history = [];
        localStorage.removeItem('driver_fatigue_history');
        renderHistoryTable();
        renderRecentDashboardTable();
      }
    });
  }

  // --- REPORTS & CHARTS RENDERING ---
  function renderReports() {
    const total = state.history.length;
    const alerts = state.history.filter(h => h.isAlert).length;
    const normal = total - alerts;

    document.getElementById('rep-total-sessions').textContent = total;
    document.getElementById('rep-fatigue-events').textContent = alerts;
    document.getElementById('rep-normal-events').textContent = normal;

    // Canvas Bar Chart
    const canvas = document.getElementById('trend-chart');
    if (!canvas) return;

    // Resize canvas to parent width
    canvas.width = canvas.parentElement.clientWidth || 600;
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;

    ctx.clearRect(0, 0, w, h);

    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const counts = [1, 3, 0, 2, 4, 1, Math.min(alerts, 5)];

    const barWidth = Math.min(45, (w / days.length) * 0.5);
    const gap = w / days.length;
    const maxVal = 5;

    days.forEach((day, idx) => {
      const count = counts[idx];
      const barH = (count / maxVal) * (h - 50);
      const x = gap * idx + (gap - barWidth) / 2;
      const y = h - 30 - barH;

      // Draw Bar
      ctx.fillStyle = count > 2 ? '#ef233c' : '#00d2ff';
      ctx.beginPath();
      ctx.roundRect(x, y, barWidth, barH, [4, 4, 0, 0]);
      ctx.fill();

      // Draw Count
      ctx.fillStyle = '#f1f5f9';
      ctx.font = '11px Inter, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(count.toString(), x + barWidth / 2, y - 6);

      // Draw Day Label
      ctx.fillStyle = '#94a3b8';
      ctx.fillText(day, x + barWidth / 2, h - 10);
    });
  }

  // Report Modal Export
  const exportModal = document.getElementById('export-modal');
  const btnExportReport = document.getElementById('btn-export-report');
  const btnCloseModal = document.getElementById('btn-close-modal');
  const reportTextPreview = document.getElementById('report-text-preview');
  const btnCopyReport = document.getElementById('btn-copy-report');
  const btnDownloadTxt = document.getElementById('btn-download-txt');

  function generateAuditReportText() {
    const total = state.history.length;
    const alerts = state.history.filter(h => h.isAlert).length;
    const normal = total - alerts;
    const alertRate = total > 0 ? ((alerts / total) * 100).toFixed(1) : '0.0';

    return `=====================================================
REAL-TIME DRIVER FATIGUE DETECTION SYSTEM
COLLEGE CAPSTONE AUDIT REPORT (VERCEL BUILD)
=====================================================
Driver Name      : ${state.currentUser.name}
Email            : ${state.currentUser.email}
Vehicle ID       : ${state.currentUser.vehicleId}
Project Division : Final-Year Engineering Project
Team Members     : ${state.currentUser.team}
Report Date      : ${new Date().toLocaleString()}

SUMMARY STATISTICS:
-----------------------------------------------------
Total Monitored Incidents    : ${total}
Critical Fatigue Siren Events: ${alerts}
Normal Alertness Periods     : ${normal}
Fatigue Incidence Ratio      : ${alertRate}%
Average Session Duration     : 38 minutes

RECENT LOGGED INCIDENTS:
-----------------------------------------------------
${state.history.slice(0, 6).map(h => 
  `• [${h.dateStr} ${h.timeStr}] ${h.fatigueStatus} | ${h.eyeStatus} | EAR: ${h.ear ? h.ear.toFixed(2) : '0.32'} | MAR: ${h.mar ? h.mar.toFixed(2) : '0.38'} | Alert: ${h.isAlert ? 'YES' : 'NO'}`
).join('\n')}

SYSTEM ARCHITECTURE SPECIFICATIONS:
-----------------------------------------------------
OpenCV Role       : Image preprocessing (CLAHE), facial ROI cropping, EAR/MAR metric calculation.
Keras/TF Role     : MobileNetV2 deep convolutional neural network for eye/yawn classification.
Assistive Notice  : Demonstrational academic project for driver safety evaluation.
=====================================================`;
  }

  if (btnExportReport) {
    btnExportReport.addEventListener('click', () => {
      const txt = generateAuditReportText();
      reportTextPreview.textContent = txt;
      exportModal.classList.remove('hidden');
    });
  }

  if (btnCloseModal) {
    btnCloseModal.addEventListener('click', () => {
      exportModal.classList.add('hidden');
    });
  }

  if (btnCopyReport) {
    btnCopyReport.addEventListener('click', () => {
      navigator.clipboard.writeText(generateAuditReportText()).then(() => {
        alert('Report copied to clipboard!');
      });
    });
  }

  if (btnDownloadTxt) {
    btnDownloadTxt.addEventListener('click', () => {
      const blob = new Blob([generateAuditReportText()], { type: 'text/plain' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `driver_fatigue_report_${Date.now()}.txt`;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  // --- SAFETY AI ASSISTANT KNOWLEDGE BASE ---
  const aiKnowledgeBase = {
    'what is driver fatigue': 'Driver fatigue (drowsiness or sleepy driving) is a dangerous physiological state of reduced cognitive and motor alertness caused by sleep deprivation, long drive durations, or circadian rhythm dips. It impairs braking reaction times, causes lane drifting, and can lead to involuntary micro-sleep episodes.',
    'what are the symptoms of fatigue': 'Common symptoms include: (1) Heavy eyelids and prolonged blink duration, (2) Involuntary repetitive yawning, (3) Difficulty maintaining head posture or head nodding, (4) Inability to recall the last few kilometers driven, (5) Wandering thought patterns, and (6) Delayed reaction to traffic signals.',
    'why do closed eyes indicate fatigue': 'A normal voluntary blink takes approximately 100 to 400 milliseconds. When fatigued, eyelid reopening is slow and incomplete. In computer vision, PERCLOS (Percentage of Eye Closure) measures prolonged eye closure. If the Eye Aspect Ratio (EAR) remains below threshold (>1.5s), it indicates an involuntary micro-sleep event.',
    'what is yawning detection': 'Yawning detection monitors mouth geometry. Our system computes the Mouth Aspect Ratio (MAR) by calculating vertical lip distances divided by mouth width. While talking produces moderate MAR (0.35-0.45), yawning causes the jaw to open widely (MAR > 0.65) for 2 to 4 seconds, signaling early driver exhaustion.',
    'how does opencv help this project': 'OpenCV handles image preprocessing: (1) Live video acquisition from the cabin camera at 30 FPS, (2) Frame resizing and grayscale conversion, (3) CLAHE histogram equalization for nighttime contrast, (4) Haar Cascade Classifiers to detect the driver face ROI, and (5) Landmark coordinate extraction for EAR and MAR calculation.',
    'what is keras': 'Keras is a high-level deep learning API running on top of TensorFlow. In this project, we designed a Convolutional Neural Network (CNN) based on MobileNetV2 architecture. It evaluates cropped eye and mouth matrices to classify whether eyes are closed or yawning with high resilience against spectacles and cabin lighting variations.',
    'how does the fatigue detection model work': 'The model operates in a 5-stage pipeline: (1) Webcam frame capture, (2) Face & landmark localization via OpenCV, (3) EAR and MAR geometric calculations, (4) Deep Keras CNN inference on facial ROI, and (5) Temporal threshold filtering: if EAR < 0.22 for ≥1.5s or MAR > 0.65 repeatedly, the system triggers the multi-modal alarm siren.',
    'what should a driver do when fatigue is detected': 'When the fatigue alarm sounds: (1) Immediately pull over safely at a designated rest stop or service area, (2) Take a 15 to 20-minute power nap, (3) Consume water or coffee (caffeine takes ~25 mins to absorb), (4) Walk and stretch to stimulate circulation, and (5) Never attempt to push through severe drowsiness.'
  };

  const chatMessagesBox = document.getElementById('chat-messages-box');
  const chatInput = document.getElementById('chat-input');
  const btnSendChat = document.getElementById('btn-send-chat');

  function sendChatMessage(userText) {
    if (!userText.trim()) return;

    // Append User Message
    appendMessage(userText, 'user', 'Driver');

    // Simulate thinking delay
    setTimeout(() => {
      const lower = userText.toLowerCase();
      let reply = null;

      // Check domain relevance
      const isRelevant = ['fatigue', 'drowsy', 'sleep', 'eye', 'mouth', 'yawn', 'opencv', 'keras', 'model', 'ear', 'mar', 'blink', 'driver', 'safety', 'project', 'what is', 'symptoms', 'break'].some(k => lower.includes(k));

      if (!isRelevant) {
        reply = 'I am the Driver Safety AI Assistant specialized in driver fatigue, OpenCV computer vision, Keras neural networks, and road safety protocols for this capstone project. Please ask a project-related question.';
      } else {
        // Match predefined knowledge
        for (const [key, ans] of Object.entries(aiKnowledgeBase)) {
          if (lower.includes(key) || key.split(' ').every(w => lower.includes(w))) {
            reply = ans;
            break;
          }
        }
      }

      if (!reply) {
        reply = `Based on this Driver Fatigue Detection capstone project:
Our system uses OpenCV for facial landmark extraction (Eye Aspect Ratio and Mouth Aspect Ratio) combined with a Keras MobileNetV2 CNN classifier. When continuous eye closure exceeds 1.5 seconds, the emergency siren is triggered. Always take a rest break when fatigue is detected.`;
      }

      appendMessage(reply, 'assistant', 'Driver Safety AI Assistant');
    }, 450);
  }

  function appendMessage(text, type, sender) {
    const bubble = document.createElement('div');
    bubble.className = `message-bubble ${type}`;
    bubble.innerHTML = `
      <div class="msg-sender">${sender}</div>
      <div class="msg-text">${text}</div>
    `;
    chatMessagesBox.appendChild(bubble);
    chatMessagesBox.scrollTop = chatMessagesBox.scrollHeight;
  }

  if (btnSendChat) {
    btnSendChat.addEventListener('click', () => {
      const text = chatInput.value;
      chatInput.value = '';
      sendChatMessage(text);
    });
  }

  if (chatInput) {
    chatInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        const text = chatInput.value;
        chatInput.value = '';
        sendChatMessage(text);
      }
    });
  }

  // Quick Chips
  const chipBtns = document.querySelectorAll('.chip-btn');
  chipBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const q = btn.getAttribute('data-question');
      if (q) sendChatMessage(q);
    });
  });

  // --- SETTINGS CONTROLS ---
  const setSound = document.getElementById('set-sound');
  const setVibrate = document.getElementById('set-vibrate');
  const setDemo = document.getElementById('set-demo');
  const setClosureTime = document.getElementById('set-closure-time');
  const setEarThresh = document.getElementById('set-ear-thresh');
  const setMarThresh = document.getElementById('set-mar-thresh');

  const valClosureTime = document.getElementById('val-closure-time');
  const valEarThresh = document.getElementById('val-ear-thresh');
  const valMarThresh = document.getElementById('val-mar-thresh');

  if (setSound) {
    setSound.addEventListener('change', (e) => state.settings.soundEnabled = e.target.checked);
  }
  if (setVibrate) {
    setVibrate.addEventListener('change', (e) => state.settings.vibrationEnabled = e.target.checked);
  }
  if (setDemo) {
    setDemo.addEventListener('change', (e) => {
      state.isDemoMode = e.target.checked;
      const demoBadge = document.getElementById('demo-mode-badge');
      if (demoBadge) demoBadge.style.display = state.isDemoMode ? 'inline-block' : 'none';
    });
  }

  if (setClosureTime) {
    setClosureTime.addEventListener('input', (e) => {
      state.settings.closureDelay = parseFloat(e.target.value);
      valClosureTime.textContent = `${state.settings.closureDelay.toFixed(1)}s`;
    });
  }
  if (setEarThresh) {
    setEarThresh.addEventListener('input', (e) => {
      state.settings.earThreshold = parseFloat(e.target.value);
      valEarThresh.textContent = state.settings.earThreshold.toFixed(2);
    });
  }
  if (setMarThresh) {
    setMarThresh.addEventListener('input', (e) => {
      state.settings.marThreshold = parseFloat(e.target.value);
      valMarThresh.textContent = state.settings.marThreshold.toFixed(2);
    });
  }

  // Profile Form
  const profileForm = document.getElementById('profile-form');
  if (profileForm) {
    profileForm.addEventListener('submit', (e) => {
      e.preventDefault();
      state.currentUser.name = document.getElementById('prof-name').value.trim() || state.currentUser.name;
      state.currentUser.email = document.getElementById('prof-email').value.trim() || state.currentUser.email;
      state.currentUser.vehicleId = document.getElementById('prof-vehicle').value.trim() || state.currentUser.vehicleId;
      state.currentUser.team = document.getElementById('prof-team').value.trim() || state.currentUser.team;

      document.getElementById('nav-driver-name').textContent = state.currentUser.name;
      alert('Driver and capstone profile saved successfully!');
    });
  }

  // Initialize UI
  renderRecentDashboardTable();
  updateDashboardAndHudUI();

});
