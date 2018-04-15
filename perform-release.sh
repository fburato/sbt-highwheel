#!/bin/bash
# Move the .gnupg folder to avoid signature clashing
# Update version in version.sbt
# Commit and tag
sbt publishSigned sonatypeRelease
# Update version in version.sbt to add the snapshot
# Commit
# Push everything