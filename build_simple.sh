#!/bin/bash

echo "Building minimal LocalLife APK..."

# Clean build directory
rm -rf build
mkdir -p build/res/values
mkdir -p build/java

# Set Android JAR path
ANDROID_JAR="/data/data/com.termux/files/usr/share/aapt/android.jar"

# Create minimal resources
echo '<?xml version="1.0" encoding="utf-8"?><resources><string name="app_name">LocalLife</string></resources>' > build/res/values/strings.xml

# Create simplified manifest
cat > build/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.locallife">
    
    <application
        android:allowBackup="true"
        android:icon="@android:drawable/ic_menu_my_calendar"
        android:label="@string/app_name"
        android:theme="@android:style/Theme.Material.Light">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

# Generate R.java
echo "Generating R.java..."
aapt package -f -m -J build/java -M build/AndroidManifest.xml -S build/res -I $ANDROID_JAR

if [ $? -ne 0 ]; then
    echo "AAPT failed!"
    exit 1
fi

# Compile Java files
echo "Compiling Java files..."
javac -cp $ANDROID_JAR -d build/java app/src/main/java/com/locallife/MainActivity.java build/java/com/locallife/R.java

if [ $? -ne 0 ]; then
    echo "Java compilation failed!"
    exit 1
fi

# Convert to DEX
echo "Converting to DEX..."
d8 --output build/classes.dex build/java/com/locallife/*.class

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
    echo ""
    echo "APK details:"
    aapt dump badging build/locallife-unsigned.apk | head -5
else
    echo "✗ APK verification failed"
    exit 1
fi