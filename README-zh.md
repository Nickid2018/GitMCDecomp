# GitMCDecomp

![GitHub](https://img.shields.io/github/license/Nickid2018/GitMCDecomp)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/Nickid2018/GitMCDecomp/listen-mc-decompile)
![GitHub top language](https://img.shields.io/github/languages/top/Nickid2018/GitMCDecomp)
![GitHub repo file count](https://img.shields.io/github/directory-file-count/Nickid2018/GitMCDecomp)

![GitHub Repo stars](https://img.shields.io/github/stars/Nickid2018/GitMCDecomp?style=social)
![GitHub forks](https://img.shields.io/github/forks/Nickid2018/GitMCDecomp?style=social)

[English](README.md) | 中文

自动生成Minecraft反编译源码并写入Git仓库的GitHub Action。

## 如何使用

### Fork此仓库并配置机密数据

首先将本仓库fork到你的仓库（下面简称执行仓库），
并且创建一个**私有**库用于存放Minecraft的源码和历史（简称存放仓库）。

为存放仓库生成Deploy Key，将私钥作为机密数据`DEPLOY_PRIVATE_KEY`存入执行仓库中。

最后，将存放仓库的完整名称作为机密数据`DESTINATION`存入执行仓库之中。

### 触发工作流执行

本仓库默认使用了`workflow_dispatch`事件触发器，需要手动触发或者通过REST API触发。
如果你无法使用上述方式进行触发，可以使用`schedule`触发器，像下面这样配置：

```yaml
# listen-mc-decompile.yml
on:
  schedule:
    - cron: '0/5 * * * *'
```

### 一次性生成所有Minecraft版本源码

可以使用[diauweb/GitMCDecomp](https://github.com/diauweb/GitMCDecomp)。