#!/bin/bash

echo "Building LocalLife APK..."

# Create build directory
mkdir -p build/java
mkdir -p build/res

# Set classpath for Android SDK
ANDROID_JAR="/data/data/com.termux/files/usr/share/aapt/android.jar"

# Copy resources
cp -r app/src/main/res/* build/res/

# Create R.java file
echo "Generating R.java..."
aapt package -f -m -J build/java -M app/src/main/AndroidManifest.xml -S build/res -I $ANDROID_JAR

# Find all Java files
find app/src/main/java -name "*.java" > build/java_files.txt
find build/java -name "*.java" >> build/java_files.txt

# Compile Java files
echo "Compiling Java files..."
javac -cp $ANDROID_JAR -d build/classes @build/java_files.txt

if [ $? -ne 0 ]; then
    echo "Java compilation failed!"
    exit 1
fi

# Convert to DEX
echo "Converting to DEX..."
d8 --output build/classes.dex build/classes/**/*.class

# Create APK
echo "Creating APK..."
aapt package -f -M app/src/main/AndroidManifest.xml -S build/res -I $ANDROID_JAR -F build/locallife-unsigned.apk

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