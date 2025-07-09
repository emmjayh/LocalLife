#!/bin/bash

echo "Building simple APK in Termux..."

# Create necessary directories
mkdir -p build/res

# Create a minimal resources file
cat > build/res/values.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
</resources>
EOF

# Package resources with aapt
aapt package -f -m -J build -M AndroidManifest.xml -I /data/data/com.termux/files/usr/share/aapt/android.jar

# Create APK with aapt
aapt package -f -M AndroidManifest.xml -I /data/data/com.termux/files/usr/share/aapt/android.jar -F build/app-unsigned.apk

# Add DEX file to APK
cd build && zip -r app-unsigned.apk ../classes.dex && cd ..

# Sign the APK
apksigner sign --ks-pass pass:android --key-pass pass:android --ks-key-alias debugkey --ks debug.keystore build/app-unsigned.apk

echo "APK built successfully: build/app-unsigned.apk"