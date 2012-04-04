#!/bin/bash

#kills all running test clients

ps -u timtest -o pid,cmd | grep '[0-9] C2MON-TEST-CLIENT' | awk '{print $1}' | xargs kill
