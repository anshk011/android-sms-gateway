# Battery Optimization

For the SMS Gateway to run reliably in the background, disable battery optimization for the app.

## Stock Android / Pixel

Settings → Apps → SMS Gateway → Battery → **Unrestricted**

## Samsung (One UI)

Settings → Apps → SMS Gateway → Battery → **Unrestricted**

Also: Settings → Device Care → Battery → Background usage limits → **Never sleeping apps** → Add SMS Gateway

## Xiaomi / MIUI

Settings → Apps → Manage apps → SMS Gateway → Battery saver → **No restrictions**

Also enable **Autostart**: Settings → Apps → Manage apps → SMS Gateway → Autostart → **On**

## OnePlus / OxygenOS

Settings → Apps → SMS Gateway → Battery → **Allow background activity**

## Huawei / EMUI

Settings → Apps → SMS Gateway → Battery → **Run in background** → **Enable**

Phone Manager → Protected apps → Enable SMS Gateway

## General

The app requests `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission on first run. Tap **Allow** when prompted for the most reliable operation.
