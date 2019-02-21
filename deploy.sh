#!/usr/bin/env bash

set -e
lein sass4clj once
lein -U deploy

