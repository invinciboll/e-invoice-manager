#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: $0 <zipfile> [output_directory]"
    exit 1
fi
dir_name="${1%.zip}"

mkdir -p "$dir_name"
unzip "$1" -d "$dir_name"
find "$dir_name" -mindepth 1 -maxdepth 1 -not -name "Examples" -exec rm -rf {} +


# Define the top-level directory
top_level="$dir_name/Examples"

# Iterate through all subdirectories in the top-level directory
find "$top_level" -type d | while read -r dir; do
    # Remove files that do not end with .pdf in the current directory
    find "$dir" -maxdepth 1 -type f ! -name "*.pdf" -exec rm -f {} \;

    # Move all .pdf files in the current directory to the top-level directory
    find "$dir" -maxdepth 1 -type f -name "*.pdf" -exec mv {} "$top_level" \;
done

# Remove any empty directories left behind
find "$top_level" -type d -empty -delete

echo "Operation completed: All PDFs are moved to $top_level, and unnecessary files/directories are removed."
