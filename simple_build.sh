#!/bin/bash

echo "Building simple LocalLife APK..."

# Create build directory
mkdir -p build

# Set Android JAR path
ANDROID_JAR="/data/data/com.termux/files/usr/share/aapt/android.jar"

# Check if android.jar exists
if [ ! -f "$ANDROID_JAR" ]; then
    echo "android.jar not found at $ANDROID_JAR"
    echo "Looking for android.jar..."
    find /data/data/com.termux/files/usr -name "android.jar" 2>/dev/null
    exit 1
fi

# Create simplified manifest
cat > build/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.locallife">
    
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    
    <application
        android:allowBackup="true"
        android:icon="@android:drawable/ic_launcher"
        android:label="LocalLife"
        android:theme="@android:style/Theme.Material.Light">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="LocalLife">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

# Create empty resources directory
mkdir -p build/res/values
echo '<?xml version="1.0" encoding="utf-8"?><resources></resources>' > build/res/values/strings.xml

# Generate R.java
echo "Generating R.java..."
aapt package -f -m -J build -M build/AndroidManifest.xml -S build/res -I $ANDROID_JAR

# Compile Java files
echo "Compiling Java files..."
javac -cp $ANDROID_JAR -d build app/src/main/java/com/locallife/MainActivity.java build/com/locallife/R.java

if [ $? -ne 0 ]; then
    echo "Java compilation failed!"
    exit 1
fi

# Convert to DEX
echo "Converting to DEX..."
d8 --output build/classes.dex build/com/locallife/*.class

# Create APK
echo "Creating APK..."
aapt package -f -M build/AndroidManifest.xml -S build/res -I $ANDROID_JAR -F build/locallife-unsigned.apk

# Add DEX file to APK
cd build
zip -r locallife-unsigned.apk classes.dex
cd ..

# Sign APK
echo "Signing APK..."
apksigner sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android --ks-key-alias debugkey build/locallife-unsigned.apk

# Verify APK
echo "Verifying APK..."
apksigner verify build/locallife-unsigned.apk

if [ $? -eq 0 ]; then
    echo "✓ LocalLife APK built successfully: build/locallife-unsigned.apk"
    ls -la build/locallife-unsigned.apk
else
    echo "✗ APK verification failed"
    exit 1
fi