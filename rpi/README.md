## Installation of raspberry

Install jar in `/var/opt/brewer`

Add service file to `/etc/systemd/system/brewer.service`

Run `sudo systemctl enable brewer.service`
Run `sudo systemctl start brewer.service`

To see log output, run `sudo journalctl -u brewer.service`