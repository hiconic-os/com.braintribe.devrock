#!/bin/bash

# Target directory where you want to extract files
TARGET_DIR="extracted"

# Check if TARGET_DIR exists, if not create it
if [ ! -d "$TARGET_DIR" ]; then
    mkdir -p "$TARGET_DIR"
fi

# Loop through each .zip file in the current directory
for zipfile in *.zip; do
    # Check if the file is a regular file
    if [ -f "$zipfile" ]; then
        # Extract the zip file into the target directory
        unzip -q "$zipfile" -d "$TARGET_DIR"
    fi
done

echo "Extraction complete."