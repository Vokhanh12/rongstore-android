#!/bin/bash
set -e

OUT=generated/iam-client
SPEC=../../rongstore-system/apis/openapi/iam/v1/services/iam_service.swagger.json

echo "🚀 Generate IAM client"

rm -rf $OUT

openapi-generator generate \
  -i $SPEC \
  -g kotlin \
  -o $OUT \
  --library jvm-retrofit2 \
  --additional-properties=\
packageName=com.aliasadi.iam.client,\
apiPackage=com.aliasadi.iam.client.api,\
modelPackage=com.aliasadi.iam.client.dto,\
useCoroutines=true,\
serializationLibrary=moshi

echo "🧹 Cleaning gradle files..."

rm -rf $OUT/{gradle,gradlew,gradlew.bat,settings.gradle}

BUILD_GRADLE="$OUT/build.gradle"

sed -i '' '/^publishing {/,/^}/d' "$BUILD_GRADLE"
sed -i '' '/^spotless {/,/^}/d' "$BUILD_GRADLE"
sed -i '' '/^java {/,/^}/d' "$BUILD_GRADLE"
sed -i '' '/^test {/,/^}/d' "$BUILD_GRADLE"
sed -i '' '/^wrapper {/,/^}/d' "$BUILD_GRADLE"

sed -i '' \
  "s/apply plugin: 'kotlin'/apply plugin: 'kotlin'\napply plugin: 'java-library'/" \
  "$BUILD_GRADLE"

# Ensure JVM compatibility with Android
cat <<EOF >> "$BUILD_GRADLE"

kotlin {
    jvmToolchain(17)
}
EOF

echo "✅ IAM client ready"
