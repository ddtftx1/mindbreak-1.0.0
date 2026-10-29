#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
mindbreak (精神污染) - client asset generator.

Generates (repeatably, overwriting outputs):
  src/main/resources/assets/mindbreak/textures/item/*.png   5x 16x16 RGBA item sprites
  src/main/resources/assets/mindbreak/icon.png              128x128 RGBA mod icon
  src/main/resources/assets/mindbreak/models/item/*.json    5 item model jsons
  src/main/resources/assets/mindbreak/lang/zh_cn.json, en_us.json
  assets-src/preview.png                                   8x nearest-neighbour preview strip

Run:  <python with Pillow> assets-src/gen_assets.py
"""

import json
import math
import os
import random

from PIL import Image, ImageDraw, ImageFont

ROOT = r"D:\dsh\mindbreak-mod"
RES = os.path.join(ROOT, "src", "main", "resources", "assets", "mindbreak")
SRC = os.path.join(ROOT, "assets-src")

ITEM_DIR = os.path.join(RES, "textures", "item")
MODEL_DIR = os.path.join(RES, "models", "item")
LANG_DIR = os.path.join(RES, "lang")

TRANSPARENT = (0, 0, 0, 0)


# ---------------------------------------------------------------- helpers ---
def C(hexstr, a=255):
    """'8b5a2b' -> (139, 90, 43, a)"""
    h = hexstr.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def canvas(w=16, h=16):
    return Image.new("RGBA", (w, h), TRANSPARENT)


def add_outline(img, color):
    """1px dark outline on every transparent pixel 8-adjacent to a filled one."""
    px = img.load()
    w, h = img.size
    targets = []
    for y in range(h):
        for x in range(w):
            if px[x, y][3] != 0:
                continue
            hit = False
            for dy in (-1, 0, 1):
                for dx in (-1, 0, 1):
                    if dx == 0 and dy == 0:
                        continue
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < w and 0 <= ny < h and px[nx, ny][3] != 0:
                        hit = True
                        break
                if hit:
                    break
            if hit:
                targets.append((x, y))
    for x, y in targets:
        px[x, y] = color


def paint(img, rows, palette):
    """Paint a 16-line ASCII pixel map; '.' is transparent."""
    px = img.load()
    if len(rows) != 16:
        raise ValueError("expected 16 rows, got %d" % len(rows))
    for y, row in enumerate(rows):
        if len(row) != 16:
            raise ValueError("row %d has length %d (%r)" % (y, len(row), row))
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            col = palette.get(ch)
            if col is None:
                raise ValueError("unknown palette char %r at (%d,%d)" % (ch, x, y))
            px[x, y] = col


def save_png(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, "PNG")


# ------------------------------------------------------- A1 absurd stick ---
def gen_absurd_stick():
    img = canvas()
    outline = C("2a1608")
    base = C("8b5a2b")
    hi = C("c9965e")
    # diagonal 2px-wide wooden stick, bottom-left -> top-right
    for y in range(3, 13):
        x = 14 - y
        img.putpixel((x, y), base)
        img.putpixel((x + 1, y), hi if 4 <= y <= 11 else base)
    add_outline(img, outline)
    sparks = [
        (2, 2, C("3ce8ff")), (3, 2, C("3ce8ff")),
        (1, 6, C("ff3bd4")), (2, 6, C("ff3bd4")), (1, 7, C("b81e94")),
        (14, 4, C("ff3bd4")), (14, 5, C("ff3bd4")),
        (14, 12, C("3ce8ff")), (14, 13, C("3ce8ff")),
        (4, 14, C("3ce8ff")), (5, 14, C("3ce8ff")),
        (11, 14, C("ff3bd4")), (12, 14, C("ff3bd4")),
    ]
    for x, y, col in sparks:
        if 0 <= x < 16 and 0 <= y < 16 and img.getpixel((x, y))[3] == 0:
            img.putpixel((x, y), col)
    return img


# ------------------------------------------------------- A2 curse book -----
def gen_curse_book():
    img = canvas()
    pal = {
        "@": C("150a20"),   # outline
        "D": C("2a1240"),   # spine dark
        "P": C("4a2270"),   # cover purple
        "L": C("6b3499"),   # cover light
        "G": C("45ff7a"),   # bright rune green
        "g": C("1e8a45"),   # dim green glow
        "K": C("0a1f10"),   # mouth interior
        "C": C("efe6c8"),   # pages light
        "S": C("c4b48c"),   # pages shade
    }
    rows = [
        "................",
        "................",
        "...@@@@@@@@@@...",
        "...@DDPLLPPP@...",
        "...@DDPGPPPG@...",
        "...@DDgGGGGg@...",
        "...@DDgGKKGg@...",
        "...@DDgGKKGg@...",
        "...@DDgGGGGg@...",
        "...@DDPGPPPG@...",
        "...@DDPPPPPP@...",
        "...@@@@@@@@@@...",
        "...@CCCCCCCCCC@.",
        "...@SSSSSSSSSS@.",
        "...@@@@@@@@@@@@.",
        "................",
    ]
    paint(img, rows, pal)
    return img


# ------------------------------------------------------ A3 chaos pearl ----
def gen_chaos_pearl():
    img = canvas()
    cx = cy = 7.5
    rmax = 5.6
    dark = {
        0: C("4a1578"),  # purple
        1: C("157a3e"),  # green
        2: C("167a8c"),  # cyan
    }
    mid = {
        0: C("8a3ad6"),
        1: C("3bf07e"),
        2: C("3be0ff"),
    }
    for y in range(16):
        for x in range(16):
            dx, dy = x - cx, y - cy
            d = math.hypot(dx, dy)
            if d > rmax:
                continue
            dn = d / rmax
            a = math.atan2(dy, dx) / (2.0 * math.pi) + 0.5
            t = (a * 3.0 + dn * 1.15) % 1.0
            band = int(t * 3.0) % 3
            col = dark[band] if dn > 0.70 else mid[band]
            img.putpixel((x, y), col)
    # specular highlight, upper-left
    for x, y, col in [
        (5, 4, C("eefffb")), (6, 4, C("e0fff8")),
        (4, 5, C("b9fff2")), (5, 5, C("9cffe6")),
        (6, 5, C("6fe8d8")),
    ]:
        if (x - cx) ** 2 + (y - cy) ** 2 <= rmax * rmax:
            img.putpixel((x, y), col)
    add_outline(img, C("1a0526"))
    return img


# ------------------------------------------------------- A4 regret cake ---
def gen_regret_cake():
    img = canvas()
    pal = {
        "@": C("1a1008"),   # outline
        "F": C("f0e8d6"),   # frosting (cream white; keeps the bone-white skull readable)
        "f": C("d8ccb0"),   # frosting shade
        "W": C("ffffff"),   # bone/skull white
        "w": C("dcd6c8"),   # skull shade
        "K": C("14100c"),   # eye holes
        "B": C("cc9e5e"),   # sponge
        "b": C("a87b3f"),   # sponge shade
        "d": C("7a5527"),   # sponge dark
        "M": C("5fb042"),   # mould
        "m": C("3a782c"),   # mould dark
    }
    rows = [
        "................",
        "................",
        "................",
        ".....@@@@@......",
        "....@WWWWW@.....",
        "....@WKWKW@.....",
        "....@WWKWW@.....",
        "....@@@@@@@.....",
        "..@FFFFFFFFFF@..",
        "..@FFFFFFFFFF@..",
        "..@FBBBfBBBBF@..",
        "..@BdbBBdbBbB@..",
        "..@mdBBdbBbBm@..",
        "..@MMmMMMmMMm@..",
        "..@@@@@@@@@@@@..",
        "................",
    ]
    paint(img, rows, pal)
    return img


# ------------------------------------------------------ A5 mystery meat ---
def gen_mystery_meat():
    img = canvas()
    OUTLINE = C("1a0e0c")
    R = C("b03a3a")   # raw red
    r = C("7e2626")   # dark red
    V = C("d45a50")   # bright red
    G = C("6fbf3f")   # suspicious green
    g = C("3e7a22")   # dark suspicious green
    W = C("f0ead6")   # bone
    w = C("c8bfa4")   # bone shade

    extents = {
        2: (5, 8), 3: (4, 9), 4: (3, 10), 5: (2, 10), 6: (2, 11),
        7: (2, 11), 8: (2, 11), 9: (2, 10), 10: (2, 10), 11: (2, 9),
        12: (3, 8), 13: (4, 7),
    }
    blob = set()
    for y, (x0, x1) in extents.items():
        for x in range(x0, x1 + 1):
            blob.add((x, y))
            if x in (x0, x1) or y in (2, 13) or y >= 12:
                col = r
            elif (x * 3 + y * 5) % 11 == 0:
                col = V
            elif (x + y * 2) % 7 == 0:
                col = r
            else:
                col = R
            img.putpixel((x, y), col)

    # green mould spots (G with a g shadow pixel where possible)
    spots = [(5, 4), (6, 4), (5, 5), (9, 7), (8, 8), (9, 8), (4, 9), (6, 11), (5, 12)]
    spot_set = set(spots)
    for x, y in spots:
        if (x, y) in blob:
            img.putpixel((x, y), G)
            for dx, dy in ((1, 0), (0, 1)):
                n = (x + dx, y + dy)
                if n in blob and n not in spot_set:
                    img.putpixel(n, g)

    # a small bone sticking out on the right
    for x, y in [(9, 10), (10, 10), (11, 10), (12, 10)]:
        img.putpixel((x, y), W)
    img.putpixel((12, 10), w)
    img.putpixel((13, 10), W)
    img.putpixel((13, 9), w)
    img.putpixel((13, 11), w)

    add_outline(img, OUTLINE)
    return img


# ------------------------------------------------------------ B mod icon --
def gen_icon():
    S = 32
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    px = img.load()
    purples = [C("2e0c42"), C("4a1668"), C("7a24b4"), C("b03cf0")]
    cx = cy = 15.5
    for y in range(S):
        for x in range(S):
            dx, dy = x - cx, y - cy
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx) / (2.0 * math.pi) + 0.5
            d = r / 21.0
            t = (a * 2.0 + d * 1.7) % 1.0
            col = purples[int(t * 4) % 4]
            if r > 13.5:  # vignette towards the corners
                f = max(0.22, 1.0 - (r - 13.5) / 9.0 * 0.7)
                col = (int(col[0] * f), int(col[1] * f), int(col[2] * f), 255)
            px[x, y] = col

    rng = random.Random(20261029)
    for _ in range(16):  # neon chaos specks
        x, y = rng.randrange(S), rng.randrange(S)
        px[x, y] = C("e63bff") if rng.random() < 0.5 else C("3ce8ff")

    # creeper face, 8x8 pattern scaled x3, centred
    face = [
        "00000000",
        "01100110",
        "01100110",
        "00011000",
        "00111100",
        "00111100",
        "00100100",
        "00000000",
    ]
    scale = 3
    off = 4
    cells = []
    for fy, row in enumerate(face):
        for fx, ch in enumerate(row):
            if ch == "1":
                cells.append((off + fx * scale, off + fy * scale))

    G1 = C("8cff9e")  # top-left glint
    G2 = C("46f05a")  # body
    G3 = C("28a83c")  # bottom-right shade
    for bx, by in cells:
        for yy in range(scale):
            for xx in range(scale):
                if (xx, yy) == (0, 0):
                    col = G1
                elif (xx, yy) == (scale - 1, scale - 1):
                    col = G3
                else:
                    col = G2
                px[bx + xx, by + yy] = col

    # corruption creeping into the face
    for bx, by in cells:
        if rng.random() < 0.22:
            xx, yy = rng.randrange(scale), rng.randrange(scale)
            px[bx + xx, by + yy] = C("e63bff") if rng.random() < 0.6 else C("8a3ad6")

    # dark green outline around the whole face silhouette (incl. inner holes)
    face_px = set()
    for bx, by in cells:
        for yy in range(scale):
            for xx in range(scale):
                face_px.add((bx + xx, by + yy))
    dark = C("0a2a10")
    for fx, fy in face_px:
        for ddy in (-1, 0, 1):
            for ddx in (-1, 0, 1):
                nx, ny = fx + ddx, fy + ddy
                if 0 <= nx < S and 0 <= ny < S and (nx, ny) not in face_px:
                    px[nx, ny] = dark

    big = img.resize((128, 128), Image.NEAREST)
    return big


# ------------------------------------------------------------- D lang ------
LANG = {
    "zh_cn": {
        "item.mindbreak.absurd_stick": "离谱棒",
        "item.mindbreak.curse_book": "嘴臭之书",
        "item.mindbreak.chaos_pearl": "混乱珍珠",
        "item.mindbreak.regret_cake": "后悔蛋糕",
        "item.mindbreak.mystery_meat": "神秘不明肉",
    },
    "en_us": {
        "item.mindbreak.absurd_stick": "Absurd Stick",
        "item.mindbreak.curse_book": "Potty-Mouth Tome",
        "item.mindbreak.chaos_pearl": "Chaos Pearl",
        "item.mindbreak.regret_cake": "Regret Cake",
        "item.mindbreak.mystery_meat": "Mystery Meat",
    },
}

ITEMS = [
    ("absurd_stick", gen_absurd_stick),
    ("curse_book", gen_curse_book),
    ("chaos_pearl", gen_chaos_pearl),
    ("regret_cake", gen_regret_cake),
    ("mystery_meat", gen_mystery_meat),
]


# ------------------------------------------------------------- preview ----
def load_preview_font():
    """Prefer a CJK-capable Windows font; fall back to Pillow's built-in."""
    for path in ("C:/Windows/Fonts/msyh.ttc", "C:/Windows/Fonts/simhei.ttf",
                 "C:/Windows/Fonts/simsun.ttc"):
        if os.path.exists(path):
            try:
                return ImageFont.truetype(path, 18), True
            except Exception:
                pass
    try:
        return ImageFont.load_default(size=18), False
    except Exception:
        return ImageFont.load_default(), False


PREVIEW_ZH = ["离谱棒", "嘴臭之书", "混乱珍珠", "后悔蛋糕", "神秘不明肉"]
PREVIEW_EN = ["absurd_stick", "curse_book", "chaos_pearl", "regret_cake", "mystery_meat"]


def gen_preview(images):
    scale = 8
    pad = 16
    cell = 16 * scale
    label_h = 30
    w = pad + len(images) * (cell + pad)
    h = pad + cell + label_h + pad
    bg = Image.new("RGBA", (w, h), (24, 24, 28, 255))
    draw = ImageDraw.Draw(bg)
    font, cjk = load_preview_font()
    for i, img in enumerate(images):
        x = pad + i * (cell + pad)
        big = img.resize((cell, cell), Image.NEAREST)
        bg.alpha_composite(big, (x, pad))
        label = PREVIEW_ZH[i] if cjk else PREVIEW_EN[i]
        size = 1.8
        try:
            tw = draw.textlength(label, font=font)
        except Exception:
            tw = len(label) * 9 * size
        draw.text((x + (cell - tw) / 2, pad + cell + 6), label,
                  font=font, fill=(228, 228, 235, 255))
    return bg


# ------------------------------------------------------------ verify ------
MODEL_EXPECT = {
    "parent": "minecraft:item/generated",
    "textures": {"layer0": "mindbreak:item/%s"},
}
ITEM_NAMES = [n for n, _ in ITEMS]


def verify():
    ok = True
    report = []
    counts = []

    # --- item textures
    for name in ITEM_NAMES:
        path = os.path.join(ITEM_DIR, name + ".png")
        with Image.open(path) as im:
            im.load()
            size, mode = im.size, im.mode
            rgba = im.convert("RGBA")
            transparent = rgba.getchannel("A").histogram()[0]
            border_clear = all(
                rgba.getpixel((x, y))[3] == 0
                for x in range(16) for y in (0, 15)
            ) and all(
                rgba.getpixel((x, y))[3] == 0
                for y in range(16) for x in (0, 15)
            )
        good = size == (16, 16) and mode == "RGBA" and transparent > 0 and border_clear
        ok &= good
        report.append("  [%s] %-16s size=%s mode=%s transparent=%d border=%s"
                      % ("OK" if good else "FAIL", name + ".png", size, mode,
                         transparent, "clear" if border_clear else "TOUCHED"))
        counts.append(transparent)

    # --- icon
    icon_path = os.path.join(RES, "icon.png")
    with Image.open(icon_path) as im:
        im.load()
        size, mode = im.size, im.mode
        hist = im.convert("RGBA").getchannel("A").histogram()
        opaque = hist[255]
    good = size == (128, 128) and mode == "RGBA" and opaque == 128 * 128
    ok &= good
    report.append("  [%s] icon.png         size=%s mode=%s opaque=%d/%d"
                  % ("OK" if good else "FAIL", size, mode, opaque, 128 * 128))

    # --- preview
    prev_path = os.path.join(SRC, "preview.png")
    with Image.open(prev_path) as im:
        im.load()
        psize, pmode = im.size, im.mode
    good = pmode == "RGBA" and psize[0] == 16 + 5 * (128 + 16)
    ok &= good
    report.append("  [%s] preview.png      size=%s mode=%s"
                  % ("OK" if good else "FAIL", psize, pmode))

    # --- models
    for name in ITEM_NAMES:
        path = os.path.join(MODEL_DIR, name + ".json")
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
        expect = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": "mindbreak:item/%s" % name},
        }
        good = data == expect
        ok &= good
        report.append("  [%s] models/item/%s.json" % ("OK" if good else "FAIL", name))

    # --- lang
    for code, entries in LANG.items():
        path = os.path.join(LANG_DIR, code + ".json")
        raw = open(path, "rb").read()
        good = not raw.startswith(b"\xef\xbb\xbf")
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
        good = good and data == entries
        ok &= good
        report.append("  [%s] lang/%s.json (%d keys, no BOM)"
                      % ("OK" if good else "FAIL", code, len(data)))

    # --- sha256 / listing
    listing = []
    for base, _, files in os.walk(RES):
        for fn in sorted(files):
            if fn.endswith((".png", ".json")):
                p = os.path.join(base, fn)
                listing.append(os.path.relpath(p, RES).replace("\\", "/") + "  (%d bytes)"
                               % os.path.getsize(p))
    listing.sort()

    print("== verify ==")
    for line in report:
        print(line)
    print("\n== assets under assets/mindbreak/ ==")
    for line in listing:
        print("  " + line)
    print("\nRESULT:", "ALL CHECKS PASSED" if ok else "FAILURES PRESENT")
    if not ok:
        raise SystemExit(1)


# --------------------------------------------------------------- main -----
def main():
    for d in (ITEM_DIR, MODEL_DIR, LANG_DIR, SRC):
        os.makedirs(d, exist_ok=True)

    images = []
    for name, fn in ITEMS:
        img = fn()
        assert img.size == (16, 16) and img.mode == "RGBA", (name, img.size, img.mode)
        save_png(img, os.path.join(ITEM_DIR, name + ".png"))
        images.append(img)

        model = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": "mindbreak:item/%s" % name},
        }
        with open(os.path.join(MODEL_DIR, name + ".json"), "w", encoding="utf-8", newline="\n") as f:
            f.write(json.dumps(model, ensure_ascii=False, indent=2) + "\n")

    save_png(gen_icon(), os.path.join(RES, "icon.png"))

    for code, entries in LANG.items():
        with open(os.path.join(LANG_DIR, code + ".json"), "w", encoding="utf-8", newline="\n") as f:
            f.write(json.dumps(entries, ensure_ascii=False, indent=2) + "\n")

    save_png(gen_preview(images), os.path.join(SRC, "preview.png"))

    print("generated %d item textures, icon, 5 models, %d lang files, preview"
          % (len(images), len(LANG)))
    verify()


if __name__ == "__main__":
    main()