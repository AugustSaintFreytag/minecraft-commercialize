#!/usr/bin/env python3

import argparse
import os
import time
import urllib.request
import urllib.error
import hashlib

DEFAULT_MASC_SKIN_SHA = "079c82e008ed86ff26defb698b6a9f4cfa09e8c4"
DEFAULT_FEM_SKIN_SHA = "70bb32c6e9749ebb6e99cfb6701d331ce85db135"


def download_heads(names_file, count, offset, delay, workdir):
	if not os.path.isfile(names_file):
		print(f"Error: File '{names_file}' does not exist.")
		exit(1)

	os.makedirs(workdir, exist_ok=True)

	with open(names_file, 'r', encoding='utf-8') as f:
		for _ in range(offset):
			if not f.readline():
				break

		processed = 0
		for line in f:
			if count is not None and processed >= count:
				break

			player = line.strip()
			if not player:
				continue

			url = f"https://mc-heads.net/avatar/{player}"
			try:
				with urllib.request.urlopen(url, timeout=10) as response:
					content = response.read()
			except urllib.error.URLError as e:
				print(f"Failed to fetch {player}: {e}")
				continue

			filename = os.path.join(workdir, f"{player}.png")
			with open(filename, 'wb') as img_file:
				img_file.write(content)
			print(f"Saved {filename}")

			processed += 1
			time.sleep(delay)


def prune_default_skins(workdir):
	if not os.path.isdir(workdir):
		print(f"Error: Directory '{workdir}' does not exist.")
		exit(1)

	for fname in os.listdir(workdir):
		if not fname.lower().endswith('.png'):
			continue

		filepath = os.path.join(workdir, fname)
		try:
			with open(filepath, 'rb') as f:
				data = f.read()
			sha = hashlib.sha1(data).hexdigest()
		except IOError:
			continue

		if sha == DEFAULT_MASC_SKIN_SHA or sha == DEFAULT_FEM_SKIN_SHA:
			os.remove(filepath)
			print(f"Removed default skin: {filepath}")


def main():
	parser = argparse.ArgumentParser(prog='fetch-player-heads', description="Manage Minecraft player head textures")
	subparsers = parser.add_subparsers(dest='command', required=True)

	# "get" command
	get_parser = subparsers.add_parser('get', help='Download player head textures')
	get_parser.add_argument(
		"names_file",
		help="Path to text file with player names, one per line"
	)
	get_parser.add_argument(
		"-n", "--count",
		type=int,
		default=None,
		help="Number of names to process (default: all)"
	)
	get_parser.add_argument(
		"-o", "--offset",
		type=int,
		default=0,
		help="Line offset to start at (default: 0)"
	)
	get_parser.add_argument(
		"-d", "--delay",
		type=float,
		default=0.1,
		help="Delay between requests in seconds (default: 0.1)"
	)
	get_parser.add_argument(
		"-w", "--workdir",
		type=str,
		default='.',
		help="Working directory for saving images (default: current dir)"
	)

	# "prune" command
	prune_parser = subparsers.add_parser('prune', help='Remove PNGs matching default Steve skin hash')
	prune_parser.add_argument(
		"-w", "--workdir",
		type=str,
		default='.',
		help="Directory to scan for .png files (default: current dir)"
	)

	args = parser.parse_args()

	if args.command == 'get':
		download_heads(args.names_file, args.count, args.offset, args.delay, args.workdir)
	elif args.command == 'prune':
		prune_default_skins(args.workdir)


if __name__ == "__main__":
	main()