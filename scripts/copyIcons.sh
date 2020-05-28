#!/bin/bash
set -e
cd "$(dirname "$0")"

if [ $# -ne 4 ]; then
    cat <<EOF 2>&1
Usage: $0 <category> <color> <resolution> <image>

Possible categories:
$(echo ../vendor/material-design-icons/*/drawable-* | tr " " "\n" | cut -d / -f 4 | uniq | sed -e "s|^|    |g")

Possible colors:
$(echo ../vendor/material-design-icons/*/drawable-*/ic_*.png | tr " " "\n" | sed -e "s|^.*ic_.*_\([^_]*\)_[0-9x]*dp.png|    \1|g" | sort | uniq)

Possible resolutions:
$(echo ../vendor/material-design-icons/*/drawable-*/ic_*.png | tr " " "\n" | sed -e "s|^.*ic_.*_\([0-9x]*dp\).png|    \1|g" | sort -n | uniq)

Possible images:
See https://material.io/resources/icons
EOF
    exit 1
fi

found=0
for file in ../vendor/material-design-icons/$1/drawable-*/ic_$4_$2_$3.png; do
    if ! echo "$file" | grep -F "*"; then
        found=$((found+1))
        context="$(echo "$file" | cut -d / -f 5)"
        mkdir -p "../app/src/main/res/$context"
        cp "$file" "../app/src/main/res/$context"
    fi
done

if [ $found -eq 0 ]; then
    echo "Unable to find icon in Git.  Trying from the Google API..." 2>&1
    tmp="$(mktemp -d)"
    trap "rm -rf $tmp" EXIT
    wget -O "$tmp/dl.zip" "https://fonts.gstatic.com/s/i/materialicons/$4/v6/$2-android.zip"
    unzip "$tmp/dl.zip" -d "$tmp"
    for file in "$tmp"/res/drawable-*/baseline_$4_$2_$(echo "$3" | sed -e "s|dp||").png; do
        found=$((found+1))
        context="$(echo -n "$file/" | tac -s / | cut -d / -f 2)"
        mkdir -p "../app/src/main/res/$context"
        cp "$file" "../app/src/main/res/$context/ic_$4_$2_$3.png"
    done
fi

if [ $found -eq 0 ]; then
    echo "Unable to find icon anywhere." 2>&1
    exit 1
fi

echo "Copied $found icons to project."
