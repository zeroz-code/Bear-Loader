#!/usr/bin/env python3
"""
PUBG Mobile KeyAuth Loader Icon Generator

This script generates PNG icons from the vector drawables for different density buckets.
Requires: Python 3, cairosvg, Pillow

Usage:
    python scripts/generate_icons.py

This will generate:
- app/src/main/res/mipmap-mdpi/ic_launcher.png (48x48)
- app/src/main/res/mipmap-hdpi/ic_launcher.png (72x72)
- app/src/main/res/mipmap-xhdpi/ic_launcher.png (96x96)
- app/src/main/res/mipmap-xxhdpi/ic_launcher.png (144x144)
- app/src/main/res/mipmap-xxxhdpi/ic_launcher.png (192x192)

And corresponding round versions.
"""

import os
import sys
from pathlib import Path

# Icon sizes for different densities
ICON_SIZES = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

def create_svg_from_vector(size, output_svg_path):
    """Create SVG for PUBG Mobile icon at specified size"""

    # Scale factor for different sizes
    scale = size / 48.0

    svg_content = f'''<?xml version="1.0" encoding="UTF-8"?>
<svg width="{size}" height="{size}" viewBox="0 0 {size} {size}" xmlns="http://www.w3.org/2000/svg">
    <!-- Background -->
    <rect width="{size}" height="{size}" fill="#1A1A1A"/>

    <!-- Orange Gradient -->
    <polygon points="0,0 {size},{size} 0,{size}" fill="#FF6B35" opacity="0.2"/>

    <!-- Main Orange Circle -->
    <circle cx="{size/2}" cy="{size/2}" r="{16*scale}" fill="#FF6B35"/>

    <!-- Inner Dark Circle -->
    <circle cx="{size/2}" cy="{size/2}" r="{12*scale}" fill="#1A1A1A"/>

    <!-- Crosshair Vertical -->
    <rect x="{(size/2)-2*scale}" y="{16*scale}" width="{4*scale}" height="{16*scale}" fill="white"/>

    <!-- Crosshair Horizontal -->
    <rect x="{16*scale}" y="{(size/2)-2*scale}" width="{16*scale}" height="{4*scale}" fill="white"/>

    <!-- Center Dot -->
    <circle cx="{size/2}" cy="{size/2}" r="{2*scale}" fill="#FF6B35"/>

    <!-- Corner Tactical Marks -->
    <rect x="{18*scale}" y="{18*scale}" width="{2*scale}" height="{1*scale}" fill="white"/>
    <rect x="{18*scale}" y="{18*scale}" width="{1*scale}" height="{2*scale}" fill="white"/>

    <rect x="{28*scale}" y="{18*scale}" width="{2*scale}" height="{1*scale}" fill="white"/>
    <rect x="{29*scale}" y="{18*scale}" width="{1*scale}" height="{2*scale}" fill="white"/>

    <rect x="{18*scale}" y="{28*scale}" width="{1*scale}" height="{2*scale}" fill="white"/>
    <rect x="{18*scale}" y="{29*scale}" width="{2*scale}" height="{1*scale}" fill="white"/>

    <rect x="{29*scale}" y="{28*scale}" width="{1*scale}" height="{2*scale}" fill="white"/>
    <rect x="{28*scale}" y="{29*scale}" width="{2*scale}" height="{1*scale}" fill="white"/>

    <!-- KeyAuth Branding Dots -->
    <circle cx="{20*scale}" cy="{40*scale}" r="{1*scale}" fill="#FF6B35"/>
    <circle cx="{22*scale}" cy="{40*scale}" r="{1*scale}" fill="#FF6B35"/>
    <circle cx="{24*scale}" cy="{40*scale}" r="{1*scale}" fill="#FF6B35"/>
    <circle cx="{26*scale}" cy="{40*scale}" r="{1*scale}" fill="#FF6B35"/>
    <circle cx="{28*scale}" cy="{40*scale}" r="{1*scale}" fill="#FF6B35"/>
</svg>'''

    with open(output_svg_path, 'w') as f:
        f.write(svg_content)

def generate_png_icons():
    """Generate PNG icons for all density buckets"""

    # Check if required libraries are available
    try:
        import cairosvg
    except ImportError:
        print("Error: cairosvg not found.")
        print("Please install: pip install cairosvg")
        return False

    # Generate icons for each density
    for density, size in ICON_SIZES.items():
        # Create directory if it doesn't exist
        mipmap_dir = f"app/src/main/res/mipmap-{density}"
        os.makedirs(mipmap_dir, exist_ok=True)

        # Create temporary SVG for this size
        temp_svg = f"temp_icon_{density}.svg"
        create_svg_from_vector(size, temp_svg)

        # Generate regular icon
        png_path = f"{mipmap_dir}/ic_launcher.png"
        cairosvg.svg2png(url=temp_svg, write_to=png_path, output_width=size, output_height=size)

        # Generate round icon (same design for now)
        round_png_path = f"{mipmap_dir}/ic_launcher_round.png"
        cairosvg.svg2png(url=temp_svg, write_to=round_png_path, output_width=size, output_height=size)

        # Clean up temporary SVG
        os.remove(temp_svg)

        print(f"Generated {density} icons: {size}x{size}px")

    print("Icon generation complete!")
    return True

def main():
    """Main function"""
    print("PUBG Mobile KeyAuth Loader Icon Generator")
    print("=" * 50)
    
    # Check if we're in the right directory
    if not os.path.exists("app/src/main/res"):
        print("Error: Please run this script from the project root directory")
        sys.exit(1)
    
    # Generate icons
    if generate_png_icons():
        print("\nSuccess! PNG icons generated for all density buckets.")
        print("The adaptive icons will automatically use the vector drawables on Android 8.0+")
        print("and fall back to PNG icons on older devices.")
    else:
        print("\nFailed to generate icons. Please check the error messages above.")
        sys.exit(1)

if __name__ == "__main__":
    main()
