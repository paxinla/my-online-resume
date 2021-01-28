#!/usr/bin/env python
#coding=utf-8


class FontElements(object):
    def __init__(self):
        self.font_map = {'A': "c-ed872",
                         'B': "c-cb686",
                         'C': "c-c3a74",
                         'D': "c-f0d2c",
                         'E': "c-eb67b",
                         'F': "c-d7ab7",
                         'G': "c-fc9e2",
                         'H': "c-a6391",
                         'I': "c-def89",
                         'J': "c-abd22",
                         'K': "c-e0d9d",
                         'L': "c-cdd42",
                         'M': "c-ea784",
                         'N': "c-fb41a",
                         'O': "c-d8e45",
                         'P': "c-ef0b8",
                         'Q': "c-ff407",
                         'R': "c-c6169",
                         'S': "c-ddb8d",
                         'T': "c-d5a8e",
                         'U': "c-dce83",
                         'V': "c-f4e32",
                         'W': "c-b497f",
                         'X': "c-fc0e5",
                         '欣': "z-nm2nd",
                         'Y': "c-c003b",
                         'Z': "c-e5820",
                         '@': "c-d277d",
                         '.': "c-d090e",
                         '-': "c-d058d",
                         '_': "c-f8a51",
                         'a': "c-cf018",
                         'b': "c-a989e",
                         'c': "c-d9f10",
                         'd': "c-db7bc",
                         'e': "c-c18d7",
                         'f': "c-c2dee",
                         'g': "c-c8d3b",
                         'h': "c-c000e",
                         'i': "c-dff51",
                         'j': "c-b6688",
                         'k': "c-dc5de",
                         'l': "c-bc8e8",
                         '磊': "z-nm3rd",
                         'm': "c-f9848",
                         'n': "c-f9653",
                         'o': "c-b9ce1",
                         'p': "c-b7464",
                         '潘': "z-nm1st",
                         'q': "c-eb5f5",
                         'r': "c-b64e6",
                         's': "c-f6498",
                         't': "c-ce33c",
                         'u': "c-e1fdd",
                         'v': "c-c0c87",
                         'w': "c-cdefa",
                         'x': "c-a41b5",
                         'y': "c-b1bf1",
                         'z': "c-ff9f4",
                         '0': "n-d5196",
                         '1': "n-d44ca",
                         '2': "n-cebe7",
                         '3': "n-e79a7",
                         '4': "n-c58ad",
                         '5': "n-b42ef",
                         '6': "n-a7ad2",
                         '7': "n-ac1d9",
                         '8': "n-d27d7",
                         '9': "n-b7438"}

    def _make_font_element(self, raw_character):
        if raw_character not in self.font_map:
            return raw_character
        return '<span class="{}"></span>'.format(self.font_map[raw_character])

    def convert_string(self, raw_str):
        origin = raw_str.strip()
        rs = []
        for ch in origin:
            rs.append(self._make_font_element(ch))
        return ''.join(rs)
