# Git Repository Reference

## Repository Information

**Location:** `/Users/marcoss2/Documents/DemoApp`  
**Branch:** `main`  
**Initial Commit:** c0547e1  
**Remote:** https://github.com/zzmarczz/e-commerceDemo.git  
**Tracking:** main → origin/main

## Repository Status

```bash
# View current status
git status

# View commit history
git log

# View commit history (one line per commit)
git log --oneline

# View detailed commit history
git log --stat
```

## Common Git Commands

### Viewing Changes

```bash
# See what files have changed
git status

# See differences in working directory
git diff

# See differences for staged files
git diff --cached

# View file history
git log --follow <file>
```

### Making Changes

```bash
# Stage all changes
git add .

# Stage specific file
git add <file>

# Commit staged changes
git commit -m "Your commit message"

# Stage and commit in one step (tracked files only)
git commit -am "Your commit message"

# Amend the last commit
git commit --amend
```

### Branching

```bash
# List all branches
git branch

# Create new branch
git branch <branch-name>

# Switch to branch
git checkout <branch-name>

# Create and switch to new branch
git checkout -b <branch-name>

# Delete branch
git branch -d <branch-name>
```

### Undoing Changes

```bash
# Discard changes in working directory
git checkout -- <file>

# Unstage file (keep changes)
git reset HEAD <file>

# Undo last commit (keep changes staged)
git reset --soft HEAD~1

# Undo last commit (keep changes unstaged)
git reset HEAD~1

# Undo last commit (discard changes) - CAREFUL!
git reset --hard HEAD~1
```

## Project-Specific Workflow

### After Making Code Changes

```bash
# 1. Check what changed
git status

# 2. Stage your changes
git add .

# 3. Commit with descriptive message
git commit -m "Added new feature: XYZ"

# 4. View your commit
git log --oneline -1
```

### Creating Feature Branches

```bash
# Create branch for new feature
git checkout -b feature/load-generator-enhancement

# Make your changes, then commit
git add .
git commit -m "Enhanced load generator with custom patterns"

# Switch back to main
git checkout main

# Merge feature (if desired)
git merge feature/load-generator-enhancement
```

### Viewing Project History

```bash
# See all commits with details
git log --graph --decorate --oneline

# See commits with file changes
git log --stat

# See commits for specific file
git log -- api-gateway/src/main/java/com/demo/gateway/ApiGatewayApplication.java

# See who changed what in a file
git blame <file>
```

## .gitignore Details

The following are automatically ignored:

```
# Build artifacts
target/
*.jar
*.war
*.ear

# IDE files
.idea/
*.iml
.vscode/
.settings/

# Runtime files
logs/
*.log
.pids
*.db

# OS files
.DS_Store
```

## Useful Git Aliases (Optional)

Add these to `~/.gitconfig`:

```ini
[alias]
    st = status
    co = checkout
    br = branch
    ci = commit
    unstage = reset HEAD --
    last = log -1 HEAD
    visual = log --graph --oneline --decorate --all
    amend = commit --amend --no-edit
```

Then use like: `git st` instead of `git status`

## Tagging Releases

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Initial release with load generator"

# List all tags
git tag

# Show tag details
git show v1.0.0

# Create lightweight tag
git tag v1.0.0

# Delete tag
git tag -d v1.0.0
```

## Remote Repository Setup

### GitHub (Already Configured)

**Remote URL:** https://github.com/zzmarczz/e-commerceDemo.git

```bash
# View remotes
git remote -v

# Push changes
git push

# Pull changes
git pull

# Fetch from remote
git fetch origin
```

### Adding Additional Remotes

```bash
# Add another remote
git remote add upstream <another-repo-url>

# Remove a remote
git remote remove <name>

# Rename a remote
git remote rename <old-name> <new-name>
```

### GitLab or Bitbucket

```bash
# Add remote
git remote add origin <your-repo-url>

# Push to remote
git push -u origin main
```

## Checking Out Previous Versions

```bash
# View commit history
git log --oneline

# Check out specific commit (detached HEAD)
git checkout <commit-hash>

# Return to latest
git checkout main

# Create branch from previous commit
git checkout -b recovery-branch <commit-hash>
```

## Stashing Changes

```bash
# Temporarily save changes
git stash

# List stashed changes
git stash list

# Apply stashed changes
git stash apply

# Apply and remove from stash
git stash pop

# Remove stashed changes
git stash drop
```

## Comparing Versions

```bash
# Compare working directory to last commit
git diff

# Compare two commits
git diff <commit1> <commit2>

# Compare branches
git diff main feature-branch

# Compare specific file across commits
git diff <commit1>:<file> <commit2>:<file>
```

## Repository Maintenance

```bash
# Clean untracked files (dry run)
git clean -n

# Clean untracked files (actually delete)
git clean -f

# Clean untracked files and directories
git clean -fd

# Verify repository integrity
git fsck

# Optimize repository
git gc
```

## Quick Reference Card

| Command | Description |
|---------|-------------|
| `git status` | Show working tree status |
| `git add .` | Stage all changes |
| `git commit -m "msg"` | Commit staged changes |
| `git log` | Show commit history |
| `git diff` | Show changes |
| `git branch` | List branches |
| `git checkout <branch>` | Switch branch |
| `git merge <branch>` | Merge branch |
| `git pull` | Fetch and merge from remote |
| `git push` | Push to remote |

## Tips

1. **Commit Often**: Small, frequent commits are better than large ones
2. **Write Good Messages**: Describe what and why, not how
3. **Use Branches**: Experiment safely with branches
4. **Review Before Commit**: Always check `git status` and `git diff`
5. **Don't Commit Secrets**: Never commit passwords, API keys, etc.

## Current Repository Structure

```
DemoApp/ (git repository root)
├── .git/                    (Git metadata)
├── .gitignore              (Ignored files)
├── pom.xml                 (Parent POM)
├── product-service/        (Tracked)
├── cart-service/           (Tracked)
├── order-service/          (Tracked)
├── api-gateway/            (Tracked)
├── load-generator/         (Tracked)
├── *.sh                    (Shell scripts - tracked)
├── *.md                    (Documentation - tracked)
├── logs/                   (Ignored)
├── target/                 (Ignored)
└── .pids                   (Ignored)
```

## Getting Help

```bash
# General help
git help

# Help for specific command
git help <command>
git <command> --help

# Quick reference
git <command> -h
```

## Initial Commit Details

**Commit Hash:** c0547e1  
**Message:** Initial commit: E-Commerce Demo Application with APM Load Generator  
**Files:** 56 files  
**Changes:** 5,897 insertions  
**Date:** $(git log -1 --format=%cd)

---

For more information: https://git-scm.com/doc

