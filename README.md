# Real-Time Driver Fatigue Detection System
### College Final-Year Engineering Capstone Project

A modern, responsive web application for **Real-Time Driver Fatigue Detection** designed with an automotive cockpit aesthetic. Built using pure **HTML5, CSS3, and JavaScript**, making it 100% serverless, zero-dependency, and instantly deployable to **Vercel** and **GitHub Pages**.

---

## 🚀 Live Vercel Deployment

Deploy directly to Vercel with zero configuration:
1. Push this repository to GitHub.
2. In [Vercel Dashboard](https://vercel.com), click **Add New Project**.
3. Import this repository.
4. Leave all build and output settings as default (`index.html` is in the root directory).
5. Click **Deploy**. Your live URL will be active immediately.

---

## 📂 Project Structure

```
├── index.html          # Main application entry point
├── styles.css          # Modern, responsive automotive cockpit stylesheet
├── app.js              # Complete interactive client-side engine & simulation
├── vercel.json         # Vercel deployment configuration
└── README.md           # Documentation and setup guide
```

---

## 🌟 Key Application Features

### 1. Authentication & Driver Profile
- Input validation with real-time validation error indicators.
- Quick-fill **Demo Credentials**: `driver@college.edu` / `driver123`.
- Driver profile registration with Vehicle Registration ID and Capstone team metadata.

### 2. Executive Dashboard
- Real-time physiological telemetry (Driver Monitoring Status, Fatigue Threat Level, EAR, MAR, Confidence %).
- Total monitoring duration chronometer (HH:MM:SS).
- Recent detection incident log with instant link to complete history.

### 3. Real-Time Driver Monitoring HUD
- **Webcam Support**: Accesses device camera via `navigator.mediaDevices.getUserMedia`.
- **Facial Landmark HUD Canvas**:
  - Live face bounding box with tracking corners.
  - Left & right eye Region of Interest (ROI) dashed boxes.
  - Mouth Region of Interest (ROI) box.
  - Continuous Eye Aspect Ratio (EAR) and Mouth Aspect Ratio (MAR) telemetry.
- **Evaluation Simulation Scenarios**:
  1. *Normal Driving* (Optimal alertness, normal blink rate).
  2. *Micro-Sleep (Eyes Closed)* (EAR < 0.22, prolonged closure triggers emergency alarm).
  3. *Yawning Episode* (MAR > 0.65 for > 2.5s).
  4. *Gradual Drowsiness* (Heavy eyelids, slow eyelid reopening).

### 4. Multi-Modal Emergency Alert System
- Prominent visual warning: **“FATIGUE DETECTED – PLEASE TAKE A BREAK.”**
- Synthesized dual-tone siren using HTML5 **Web Audio API** (`OscillatorNode`).
- Device haptic pulse using `navigator.vibrate`.

### 5. Detection History & Audit Log
- Stored client-side in `localStorage`.
- Filter by: **All Records**, **Alerts Only**, **Normal Driving**.
- Instant search by date, time, status, or notes.
- One-click clear history option.

### 6. Reports & Fatigue Analytics
- Summary statistics: Total sessions, critical alarms, average duration.
- Interactive HTML5 Canvas 7-day fatigue incidence trend chart.
- Diurnal time-of-day hazard breakdown (morning, afternoon, evening, night).
- **Report Generator**: Generates formatted capstone audit report with one-click copy to clipboard and `.txt` file download.

### 7. Driver Safety AI Assistant
- Interactive Q&A chat for viva questions:
  - *What is driver fatigue?*
  - *What are the symptoms of fatigue?*
  - *Why do closed eyes indicate fatigue?*
  - *What is yawning detection?*
  - *How does OpenCV help this project?*
  - *What is Keras?*
  - *How does the fatigue detection model work?*
  - *What should a driver do when fatigue is detected?*
- Safe educational responses with domain boundary protection.

### 8. Machine Learning & Model Specifications
- Full technical breakdown of **OpenCV** (CLAHE, Haar cascades, landmark geometry) and **Keras/TensorFlow** (MobileNetV2 CNN classifier).
- Mathematical formulations for Eye Aspect Ratio (EAR) and Mouth Aspect Ratio (MAR).

### 9. System Settings & Sensitivity Calibration
- Slider controls for Eye Closure Delay (0.8s – 3.0s), EAR threshold (0.15 – 0.30), and MAR threshold (0.50 – 0.85).
- Toggles for audible siren, vibration, and demo simulation mode.

---

## 💻 Local Execution

To test locally without an internet connection:
- Open `index.html` directly in any modern web browser (Chrome, Edge, Firefox, Safari).
- Or run a lightweight local static server:
  ```bash
  npx serve .
  # or
  python3 -m http.server 3000
  ```
