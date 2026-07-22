# 开发者规则 (Developer Rules)

## Deletion Safety Rule (文件删除安全规则)

Whenever you need to delete any file or folder from the user's workspace, **DO NOT** delete it permanently (e.g. do not use raw Remove-Item or rm -rf). Instead, always send the deleted files/folders to the Windows Recycle Bin.

## Minecraft MOD Jar Zip Header Safety Rule (Minecraft MOD Jar 包标准修改规则)

对于任何需要修改、注入或删除 Minecraft 第三方 MOD `.jar` 包内部资源/配方/Class 文件的需求：
1. **严禁**直接使用 PowerShell 的 `[System.IO.Compression.ZipFile]::Open` 原位删除/修改 Jar 内部条目。该方式会导致底层产生 `STORED + EXT descriptor` 标头损坏，触发 Forge `securejarhandler` 的 `java.util.zip.ZipException: only DEFLATED entries can have EXT descriptor` 启动崩溃。
2. **强制使用规范方式修改**：必须使用 Python `zipfile` 模块配合 `zipfile.ZIP_DEFLATED` 压缩标准进行完整的 JAR 包重构注入，确保每一个 ZipInfo 记录标头均符合 Java `JarInputStream` 的严格校验规范。
