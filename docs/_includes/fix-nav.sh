#!/usr/bin/env bash
set -euo pipefail

# Root of the documentation folder
DOCS=docs

# Convert CamelCase or spaces to lower-kebab
kebab() {
  echo "$1" \
    | sed -E 's/([[:upper:]])/-\L\1/g' \
    | sed -E 's/[-_ ]+/-/g' \
    | sed -E 's/^-+//;s/-+$//'
}

# 1) Rewrite all .html links in docs to folder URLs
find "$DOCS" -type f -name '*.md' | while read -r file; do
  # sed script: replace [Text](Foo-Bar.html) → [Text](foo-bar/)
  sed -i -E \
    -e 's%\(([^)]+)\.html\)%(\1/)%' \
    "$file"
done

# 2) Normalize link targets and permalinks in front-matter
#    Iterate each subdirectory under docs (first-level only)
for section in "$DOCS"/*/; do
  # section=docs/intro/ or docs/install/, etc.
  secname=$(basename "$section")
  # Desired permalink: /jpf-core/<secname>/
  desired_perma="/jpf-core/$(kebab "$secname")/"

  idx="$section/index.md"
  if [[ -f "$idx" ]]; then
    # Ensure front-matter contains correct permalink
    # If permalink exists, replace it; otherwise insert below title
    if grep -q '^permalink:' "$idx"; then
      sed -i -E \
        "s|^permalink:.*|permalink: $desired_perma|" \
        "$idx"
    else
      awk -v P="$desired_perma" '
        /^---$/ { print; infm=1; next }
        infm && /^title:/ { print; print "permalink: " P; infm=0; next }
        { print }
      ' "$idx" > tmp && mv tmp "$idx"
    fi
  fi

  # Also rename file-based pages within the section to lower-kebab directories
  find "$section" -maxdepth 1 -type f -name '*.md' ! -name 'index.md' | while read -r page; do
    base=$(basename "$page" .md)
    lower=$(kebab "$base")
    # Move Foo-Bar.md → foo-bar/index.md, adjusting its frontmatter permalink
    if [[ "$base" != "$lower" ]]; then
      dest="$section/$lower/index.md"
      mkdir -p "$(dirname "$dest")"
      mv "$page" "$dest"
    else
      mkdir -p "$section/$lower"
      mv "$page" "$section/$lower/index.md"
    fi
    # Inject proper permalink for that page
    sed -i -E \
      "s|^permalink:.*|permalink: /jpf-core/${secname}/${lower}/|; t; /^title:/a permalink: /jpf-core/${secname}/${lower}/" \
      "${section}/${lower}/index.md"
  done
done

echo "Navigation links, filenames, and permalinks fixed. Please rebuild the site."
