[![license](https://img.shields.io/github/license/mashape/apistatus.svg) ](LICENSE)

# Mapcha Fork
This is a fork of [haq/mapcha](https://github.com/haq/mapcha). This fork is used for DESTROYMC.NET.

## Changes:
- Changed default config values and messages.
- Allow OPs to bypass the captcha.
- Try to send players to destination server every 5 seconds instead of only once.
- Run console command to give players the bypass permission for 24 hours using LuckPerms.

### Default config
```yaml
# The amount of tries the player will get to solve the captcha.
tries: 5

# The time limit in seconds the player has to solve the captcha.
time_limit: 60

# The server name to connect to when the user completes a captcha
# Leave empty if you don't want it to do anything
success_server: main

messages:
  # The success message the player receive after they solve the captcha.
  success: '&aSuccess! Joining main server...'

  # The retry message the player receive after they fail one of their tries.
  retry: '&cCaptcha failed, please try again! ({CURRENT}/{MAX})'

  # The fail message the user receive after they fail the captcha.
  fail: '&cCaptcha failed!'
```
