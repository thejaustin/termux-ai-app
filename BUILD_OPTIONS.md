# Free Android Build Options

## Option 1: GitHub Actions (Recommended ⭐)
**Cost**: 100% Free (2000 minutes/month on free GitHub)
**Setup Time**: 2 minutes

1. Push your code to GitHub
2. The `.github/workflows/android-build.yml` file will automatically build your app
3. Download APKs from Actions tab → Artifacts

**To use:**
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/yourusername/termux-ai-app.git
git push -u origin main
```

## Option 2: GitHub Codespaces
**Cost**: Free (60 hours/month)
**Setup Time**: 5 minutes

1. Open your repo on GitHub
2. Click "Code" → "Codespaces" → "Create codespace"
3. Wait for setup to complete (automatic via `.devcontainer/`)
4. Run: `./gradlew assembleDebug`

## Option 3: Termux proot-distro
**Cost**: Free
**Setup Time**: 10-15 minutes

Run the provided script:
```bash
./build-in-proot.sh
```

This sets up Ubuntu in Termux and builds there with better x86_64 compatibility.

## Option 4: Gitpod
**Cost**: Free (50 hours/month)
**Setup Time**: 3 minutes

1. Go to: https://gitpod.io/#https://github.com/yourusername/termux-ai-app
2. Wait for environment setup
3. Run: `chmod +x gradlew && ./gradlew assembleDebug`

## Option 5: Online IDEs

### Replit
1. Import from GitHub at replit.com
2. Install Android SDK manually
3. Build project

### CodeSandbox
1. Import GitHub repo
2. Use terminal to build

## Fastest Option for You Right Now:

1. **GitHub Actions** - Just push to GitHub and get automatic builds
2. **proot-distro** - Try `./build-in-proot.sh` right now in Termux

## Build Commands:
- Debug APK: `./gradlew assembleDebug`
- Release APK: `./gradlew assembleRelease`
- Clean build: `./gradlew clean assembleDebug`

APKs will be in: `app/build/outputs/apk/debug/` or `app/build/outputs/apk/release/`