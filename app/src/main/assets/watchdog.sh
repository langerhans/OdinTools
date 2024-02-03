# Simple watchdog to restart our service when it gets killed
while :
do
    sleep 5
    if ! dumpsys activity services ForegroundAppWatcherService | grep -q hasBound=true
    then
        am broadcast -n de.langerhans.odintools/.tools.BootReceiver -a de.langerhans.odintools.WATCHDOG_TRIGGER
        exit 0
    fi
done