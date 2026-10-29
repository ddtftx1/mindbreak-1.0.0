#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Read-only ASCII inspector: renders generated sprites as colour-mapped text."""
import colorsys
import os
import sys

from PIL import Image

ROOT = r"D:\dsh\mindbreak-mod"
RES = os.path.join(ROOT, "src", "main", "resources", "assets", "mindbreak")
ITEM_DIR = os.path.join(RES, "textures", "item")
NAMES = ["absurd_stick", "curse_book", "chaos_pearl", "regret_cake", "mystery_meat"]


def char_for(r, g, b):
    h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
    if (r + g + b) < 90:
        return "#"  # near-black outline
    if s < 0.18:
        return "W" if v > 0.75 else ("w" if v > 0.45 else "#")
    hd = h * 360.0
    if hd < 18:
        return "R"
    if hd < 45:
        return "O" if s > 0.55 else "B"
    if hd < 62:
        return "B"
    if hd < 95:
        return "Y"
    if hd < 165:
        return "G"
    if hd < 200:
        return "C"
    if hd < 255:
        return "U"
    if hd < 320:
        return "P"
    return "M"


def dump(img, title, block=1):
    w, h = img.size
    print("\n--- %s (%dx%d) ---" % (title, w, h))
    counts = {}
    for y in range(0, h, block):
        line = []
        for x in range(0, w, block):
            p = img.getpixel((x, y))
            if p[3] == 0:
                ch = "."
            else:
                ch = char_for(p[0], p[1], p[2])
                counts[ch] = counts.get(ch, 0) + 1
            line.append(ch)
        print("".join(line))
    print("legend:", ", ".join("%s=%d" % (k, counts[k]) for k in sorted(counts)))


def main():
    for name in NAMES:
        with Image.open(os.path.join(ITEM_DIR, name + ".png")) as im:
            im = im.convert("RGBA")
            dump(im, name)
            # transparent border check
            bad = []
            for x in range(16):
                for y in (0, 15):
                    if im.getpixel((x, y))[3] != 0:
                        bad.append((x, y))
            for y in range(16):
                for x in (0, 15):
                    if im.getpixel((x, y))[3] != 0:
                        bad.append((x, y))
            print("border-transparent:", "OK" if not bad else "BAD %r" % bad[:8])

    icon = Image.open(os.path.join(RES, "icon.png")).convert("RGBA")
    dump(icon, "icon.png", block=4)  # 32x32 view of the pixel grid


if __name__ == "__main__":
    main()