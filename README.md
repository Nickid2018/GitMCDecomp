# GitMCDecomp

![GitHub](https://img.shields.io/github/license/Nickid2018/GitMCDecomp)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/Nickid2018/GitMCDecomp/listen-mc-decompile)
![GitHub top language](https://img.shields.io/github/languages/top/Nickid2018/GitMCDecomp)
![GitHub repo file count](https://img.shields.io/github/directory-file-count/Nickid2018/GitMCDecomp)

![GitHub Repo stars](https://img.shields.io/github/stars/Nickid2018/GitMCDecomp?style=social)
![GitHub forks](https://img.shields.io/github/forks/Nickid2018/GitMCDecomp?style=social)

English | [中文](README-zh_CN.md)

A GitHub Action can generate Minecraft source files and put them into your repository.

## How to use it...

### Fork repository and configure secrets

Fork this repository (Action Repository), and create a **private** repository to store
Minecraft source files (Store Repository).

Then, generate a deploy key for your Store Repository, and write the private key as
a secret called `DEPLOY_PRIVATE_KEY` in Action Repository.

Also, you should write the full name of Store Repository as a secret called
`DESTINATION` in Action Repository.

### Trigger the workflow

The repository uses `workflow_dispatch` event to trigger workflows. 
If you can't post these events, you can use `schedule` event:

```yaml
# listen-mc-decompile.yml
on:
  schedule:
    - cron: '0/5 * * * *'
```

### Create for all Minecraft versions once

See [diauweb/GitMCDecomp](https://github.com/diauweb/GitMCDecomp).