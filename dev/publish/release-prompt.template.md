You are releasing version {{VERSION}} of my Minecraft mod **TON 618 Addition** (an unofficial addon for rimuru_dev's
TON 618) for **NeoForge 1.21.1 only**: push `main` to GitHub, make the GitHub release `v{{VERSION}}`, upload the jar to
CurseForge, and update the CurseForge project's description, summary and source link. The other six loaders stay on
1.0.0 for now (they get {{VERSION}} in a later update): don't touch their branches, releases or files. Work through every
step below to the end, then give me the report at the bottom. Stop and ask me only if you are truly blocked, or if a
check below fails.

Part A needs a terminal (Git Bash) with `git` and `gh` (already signed in as Adamko-10). Part B needs my web browser,
where I am already signed in to CurseForge. If you can't use a browser, do Part A, then stop and tell me.

All texts you need are in the files named below **and** copied word for word at the end of this prompt (Appendix). If
you can read the files, use the files; if not, use the Appendix.

## Hard rules

- Never print, copy or ask for any token or password. Never sign in to anything for me. If GitHub or CurseForge asks
  for a sign-in, stop and tell me.
- **Never force-push**, never rewrite history, never delete or move existing tags or releases (the `v1.0.0*` ones
  stay). The push must be a fast-forward of `origin/main`. Push **only** `main`.
- Never commit or upload anything from `libs/` or `run/` (other people's jars: TON 618, RimuLib, Iris, Sodium and
  copies of my other mods). They are git-ignored; keep it that way.
- Don't edit code or the texts. If something is wrong, stop and tell me what.
- Don't use `git stash`.
- On CurseForge: don't delete, archive or edit older files; change nothing on the project except what Part B lists.

## Where things are

- Repository (local): `C:\Users\adam_\Documents\TON 618 Addition\ton 618 addition\` (branch `main`).
- GitHub: `Adamko-10/ton618-addition` (https://github.com/Adamko-10/ton618-addition).
- The jar: `C:\Users\adam_\Documents\TON 618 Addition\releases\{{VERSION}}\jars\{{JAR}}`
  (SHA-256 `{{SHA256}}`).
- The changelog (Markdown, for GitHub and CurseForge): `C:\Users\adam_\Documents\TON 618 Addition\releases\{{VERSION}}\changelogs\main.md`
  (the same as the repo's `dev/publish/changelog-{{VERSION}}.md`).
- The CurseForge description (Markdown): the repo's `dev/publish/curseforge-description.md`.
- The CurseForge summary (one line, under 256 characters): the repo's `dev/publish/summary.txt`.
- CurseForge project: **TON 618 Addition**, id **1807506**: https://authors.curseforge.com/#/projects/1807506 (its
  public page has a "View project" link there).

## Part A: GitHub

### A1. Checks (stop and tell me if one fails)

In the repository folder:
1. `git status --short` shows no changes to tracked files (untracked files are fine; don't add them).
2. `git rev-parse --abbrev-ref HEAD` is `main`, and `git rev-parse --short HEAD` is `{{HEAD}}` (or a later commit that
   only changes `CLAUDE.md`: check with `git diff --stat {{HEAD}} HEAD`).
3. `git fetch origin`, then `git merge-base --is-ancestor origin/main main` succeeds (the push is a fast-forward).
4. `git diff --name-only origin/main..main | grep -iE '\.jar$|^libs/|^run/'` prints nothing.
5. The jar exists, its SHA-256 is the one above (`sha256sum "<jar>"`), and its version is {{VERSION}}:
   `unzip -p "<jar>" META-INF/neoforge.mods.toml | grep -m1 '^version'` shows `version = "{{VERSION}}"`.
6. `gh release view v{{VERSION}} --repo Adamko-10/ton618-addition` says the release doesn't exist yet. If it exists,
   stop and tell me.

### A2. Push

`git push origin main` (no `--force`, no other branches).

### A3. The GitHub release

```bash
gh release create v{{VERSION}} \
  "C:/Users/adam_/Documents/TON 618 Addition/releases/{{VERSION}}/jars/{{JAR}}#{{JAR}}" \
  --repo Adamko-10/ton618-addition --target main \
  --title "TON 618 Addition {{VERSION}} [NeoForge 1.21.1]" \
  --notes-file "C:/Users/adam_/Documents/TON 618 Addition/releases/{{VERSION}}/changelogs/main.md" \
  --latest
```

(The `#{{JAR}}` part is the asset's label, so it shows the jar's real name; GitHub may turn spaces and brackets into
dots in the download's file name, which is fine.)

### A4. Check

`gh release view v{{VERSION}} --repo Adamko-10/ton618-addition`: the tag points at the pushed `main` commit, the title
is right, the notes are the changelog, the jar is attached, and it is marked **Latest**.

## Part B: CurseForge (in my browser)

### B1. Upload the file

On the project (id 1807506): **Files → Upload file**, the jar above. Set **every** field yourself: the form copies
fields from the newest older file, sometimes wrong ones, and never the relations.
- **Display name**: `TON 618 Addition {{VERSION}} [NeoForge 1.21.1]` (if the older files' display names end in `.jar`,
  match them: `{{JAR}}`).
- **Release type**: Release.
- **Game versions**: Minecraft **1.21.1** only; mod loader **NeoForge** only (remove any other loader or version the
  form copied); Java **21**; environment **Client + Server** (both).
- **Changelog**: first switch the changelog editor's mode to **Markdown**, check it switched, then paste the whole
  changelog (Appendix A) as it is. (Pasting before switching saves it as one paragraph of raw text.)
- **Relations** ("Add related project", search by name; ids don't search):
  - **TON 618** (by rimuru_dev): **Required dependency**;
  - **RimuLib** (by rimuru_dev): **Required dependency**;
  - **The Shooting Star [Demo]** (by rimuru_dev): **Optional dependency**;
  - **The Shooting Star Addition: SS-21 Quasar** (an Addons project:
    https://www.curseforge.com/minecraft/mc-addons/the-shooting-star-addition-ss-21-quasar): **Optional dependency**.
    If CurseForge can't find it, skip it and tell me in the report.
- Submit. If CurseForge answers "too many requests" (429), wait a minute and try again.

Then open `https://authors.curseforge.com/_api/project-files/1807506/files/<the new file's id>` in the same browser
and check: the display name, Release, the game versions (1.21.1, NeoForge, Java 21, Client and Server),
`changelogType` 5 (Markdown) and that the changelog has its headings and bullet lines.

### B2. The description

Project → **Description**. Replace it with the description (Appendix B), with one exception: **keep the top part
exactly as it is live**: the lines under the title with my links (the Discord server, "Thank you for your support",
the YouTube channel, "Check out my other mods"). Replace the intro paragraph above them (it now names TA-02) and
everything from the heading "⚠️ Compatibility — please read" down to the end. If the editor has a Markdown mode, use
it; if it is a rich-text editor, rebuild the same headings, bold, bullet lists and links (keep every link working).
Save, then look at the public page and check that it reads right (headings, lists, the links).

### B3. The summary

Project settings → **Summary**: the line in Appendix C (exactly; it is under 256 characters).

### B4. The source link

Project settings → **Source** (source code link): `https://github.com/Adamko-10/ton618-addition`. Leave the other
links (issues, wiki, Discord...) as they are.

## Report

Tell me, briefly:
- Part A: the pushed commit, the release's URL, and that it is Latest.
- Part B: the new CurseForge file's id and status (processing, approved...), the relations it has, and what you
  changed on the project (description, summary, source link).
- Anything you skipped or couldn't do, and why.

---

## Appendix A: the changelog (Markdown; GitHub release notes and the CurseForge file's changelog)

````markdown
{{CHANGELOG}}
````

## Appendix B: the CurseForge description (Markdown; keep the live links part at the top, see B2)

````markdown
{{DESCRIPTION}}
````

## Appendix C: the CurseForge summary

````text
{{SUMMARY}}
````
