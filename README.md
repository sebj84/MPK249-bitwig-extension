# **🎹 User Manual: Akai MPK249 for Bitwig Studio (BITWIG V6 API V25)**

This extension transforms your Akai MPK249 into a dynamic control surface for Bitwig Studio. It handles context changes (Clips, Instruments, Mixing) and significantly reduces the need for a mouse during your creative process.

---
## **🎛️ Mixing & Controls Section**

Rotary knobs (K1-K8), faders (F1-F8), and buttons (S1-S8) are organized into three banks,
selectable via the hardware Control Bank (A/B/C) buttons.
![](https://github.com/sebj84/MPK249-bitwig-extension/blob/23fc11bb9501be7fe4e4913ff54bb3872d359671/MPK%20pictures/Bank%20selection.jpg)

### **1. Knobs (K1-K8) — [CONTROL BANK A ONLY] --> Dedicated to Volume & Pan control.**

- Standard Mode: Controls the Volume of the 8 tracks in the active bank.

   - The 8 tracks shift in blocks of 8 as you navigate.

   - Note: The "Follow Cursor" option can be toggled in the controller settings within the Bitwig interface (see Capture bellow).
![](https://github.com/sebj84/MPK249-bitwig-extension/blob/23fc11bb9501be7fe4e4913ff54bb3872d359671/MPK%20pictures/cursor%20option.jpg)

- SHIFT Mode (Enter button): Controls the Panning of the 8 tracks.

![](https://github.com/sebj84/MPK249-bitwig-extension/blob/23fc11bb9501be7fe4e4913ff54bb3872d359671/MPK%20pictures/Daw%20SHIFT%20alpha.PNG)
- Visual Feedback: Track names and parameter values appear directly in Bitwig’s on-screen notifications.

### **2. Faders (F1-F8) — [CONTROL BANK A ONLY] --> Dedicated to Instrument Remote Controls.**

- Instrument Mode: The 8 faders automatically map to the 8 Remote Controls (Macros) of the selected device (VST or native Bitwig instrument).

- Auto-Follow: When you switch tracks (Up/Down arrows) or devices (Shift + Left/Right arrows), the faders instantly re-bind to the macros of the new device/effect.

### **3. S Buttons (S1-S8) — [CONTROL BANKS B & C]**

   Track management buttons are mapped as follows:

- #### BANK B + S1-S8 (Mute/Solo):

   - Simple Press: Mute / Unmute the track.

   - Shift + Press: Solo / Unsolo the track.

- #### BANK C + S1-S8 (Rec Arm):

   - Simple Press: Record Arm. Placed on Bank C to prevent accidental recordings.
---

## **🕹️ Navigation & Transport**

Located on the right side of the controller, this section handles project movement.

- 📼 **Transport**: Dedicated physical buttons for Stop, Play, Rec, Rewind, and Fast Forward.

- ⬆️ / ⬇️ **Arrows**: Select the previous or next track.

- ⬅️ / ➡️ **Arrows**:

   - Press: Select the previous or next Clip on the track.

   - Shift + Press: Navigate through Devices (instruments/effects) in the track chain. Faders update automatically to the selected device.

**- 🦶 Sustain Pedal (Smart Record):**

   - If a clip is recording: Stops recording and starts playback.

   - If not recording: Finds the first empty slot on the track and triggers a new recording.
---

## **🟦 Pad Modes (S1 to S6) — [CONTROL BANK A ONLY]**

The S1 to S6 buttons in Bank A change the functionality of the 64 pads.

### **🟢 Launchers (S1 to S5)**
- Clip Launcher (S1-S4): Controls a 4-track x 16-clip grid.

   - Use Control Bank buttons (A/B/C) to select tracks in groups of 4 (Tracks 1 to 12).

   - Use PAD Bank buttons (A/B/C/D) to move through clips in groups of 4 (Clips 1 to 16).

   - S1: Tracks 1-4 | S2: Tracks 5-8 | S3: Tracks 9-12 | S4: Tracks 13-16.

   - Colors: Green (Playing), Red (Recording), Bitwig Color (Clip present and selectable).

- Scene Launcher (S5): Pads trigger Scenes (horizontal lines). Pad colors match the Scene colors in your project.

   - Use PAD Bank (A/B/C/D) to access scenes 1 through 64.

   - Pad 1 = Scene 1, Pad 2 = Scene 2, etc.

### **🎹 Instrument & Drum Machine (S6)**
- Drum Machine: When a track with a Bitwig Drum Machine is selected, only pads containing sounds light up with the track color (Note: Feature currently in testing).

- Instruments: All pads light up in the track color to play melodies or chords.

Note:_ MIDI _data only passes through the pads when in this mode.

### **💡 Special Functions & "Shift"**
- Daw control "Enter" Button (Shift): This is your primary function key (also mapped to "Enter"). It modifies the behavior of the arrows, S buttons, and Knobs. A "Shift Active" notification appears on-screen.

- Notifications: The extension displays the Actual Track Number (e.g., "Track 12: MUTE") and the Device Name, allowing you to stay focused on the hardware.

## **☕ Support the Project**

If this script improves your workflow, feel free to buy me a coffee! Your support allows me to spend more time on updates and new features.

### **👉 Donate:**

<a href='https://ko-fi.com/Y8Y11YTWE4' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi6.png?v=6' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

## **📝 Important Notes**
- MPK Internal Settings: Ensure your MPK249's internal settings have the S Buttons set to "Momentary" and not "Toggle" for perfect synchronization with Bitwig.

- Sysex Profile: You can find my Akai MPK249 profile in the Akai sysex profile directory of this repository ([HERE](https://github.com/sebj84/MPK249-bitwig-extension/tree/6b9ce59657c7d6b72bbc14c485305ef25a65c210/Akai%20sysex%20profile)). You can upload it to your controller using the utility provided by Dobemad: https://github.com/dobemad/MPK249.
- If you want to manually redo you Keyboard mapping you can follow the Excellsheet with CC [HERE](https://github.com/sebj84/MPK249-bitwig-extension/blob/6b9ce59657c7d6b72bbc14c485305ef25a65c210/MPK249%20buttons.xlsx)

- Video Manual: Coming Soon (TODO).

**IMPORTANT**: This script is provided as-is. I cannot be held liable for any issues or damage that may affect your controller.