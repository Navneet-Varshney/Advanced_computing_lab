#!/bin/bash

# 1. Check arguments
if [ $# -ne 2 ]; then
    echo "Usage: filesRev.sh <source_dir> <destination_dir>"
    exit 1
fi

src_dir="$1"
target_dir="$2"

# 2. Check if source exists
if [ ! -d "$src_dir" ]; then
    echo "Error: Source folder '$src_dir' not found!"
    exit 1
fi

# 3. Create destination if not exists
if [ ! -d "$target_dir" ]; then
    mkdir "$target_dir"
    echo "Folder '$target_dir' created"
fi

# 4. Reverse order loop
for item in $(ls -r "$src_dir"); do

    file_path="$src_dir/$item"

    if [ -f "$file_path" ]; then

        # Get modification time
        birth_time=$(stat -c %Y "$file_path")

        # Convert to readable timestamp
        formatted_ts=$(date -d "@$birth_time" +"%Y%m%d_%H%M%S")

        # Separate filename and extension
        filename="${item%.*}"
        extension="${item##*.}"

        # Create new filename (without count)
        if [[ "$item" == *.* ]]; then
            final_name="${filename}_${formatted_ts}.${extension}"
        else
            final_name="${filename}_${formatted_ts}"
        fi

        # Copy file
        cp -p "$file_path" "$target_dir/$final_name"

        echo "Copied: $item -> $final_name"
	sleep 1
    fi
done

echo "Done: All files copied from '$src_dir' to '$target_dir' in reverse order."
