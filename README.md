# aPS3e - Pixel 7 & Tensor G2 Optimized Edition

This branch contains specific hardware-level optimizations for the **Google Pixel 7 (Tensor G2)** and **Mali-G710 GPU**. It addresses common issues like flickering textures, thermal throttling, and CPU core mismanagement.

---

## 🛠 New Features & UI Changes

### 📊 Sidebar Performance Toggles
We have added interactive switches to the `emulator_view` sidebar:
* **Turbo Mode:** Instantly toggles the frame limiter (0 for unlocked, 1 for locked).
* **Detailed Stats:** Enables a high-detail overlay showing FPS, GPU Load, and **SoC Temperature**.

### 🔍 Integrated Memory Searcher
The sidebar now includes a direct link to the native Memory Searcher tool for real-time cheat engine functionality.

---

## 🚀 Applied Optimizations

### 1. GPU Stability (Mali-G710)
To prevent the common "flicker" and "black box" bugs on Tensor chips, the following environment flags are forced:
* `MALI_USE_STRICT_SYNC`: Forces synchronized rendering to stop texture flickering.
* `APS3E_RELAXED_ZCULL_SYNC`: Fixes depth-buffer issues in complex 3D scenes.
* `APS3E_VULKAN_STRICT_RENDER_PASS`: Set to `false` to improve thermal efficiency.

### 2. CPU Threading (Tensor G2)
Tensor chips use a heterogeneous core layout (2x Big, 2x Medium, 4x Small). 
* **Thread Count:** Optimized to `4` to utilize the high-performance cores.
* **Affinity Mask:** Set to `0xF0` to prevent the OS from moving the emulator to the slow efficiency cores.

### 3. Thermal Management
* **Internal Resolution:** Defaulted to `60%` scale to maintain 60FPS without hitting thermal limits within 10 minutes of play.
* **FP16 Math:** Enabled `FORCE_LOW_PRECISION` to speed up shaders on Mali hardware.

---

## ⚙️ Configuration Reference

| Variable | Recommended Value | Description |
| :--- | :--- | :--- |
| `APS3E_TENSOR_G2_OPTIMIZATION` | `true` | Activates Pixel-specific code paths. |
| `MALI_DEBUG_DISABLE_QUIRKS` | `1` | Disables driver power-saving stutters. |
| `APS3E_MONITOR_TEMP` | `true` | Enables hardware thermal monitoring. |

---

## 📝 Developer Notes (Fixing the "100 Errors")
If the project fails to build with "100 errors," ensure:
1.  The `SwitchCompat` import has a semicolon `;`.
2.  All `set_env` logic is contained **inside** the `on_create()` method.
3.  The `Activity` implementation matches the balanced curly braces in `EmulatorActivity.java`.

