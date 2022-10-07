- Сlone this repository
```
git clone git@code.xby.icdc.io:hcl/quattro/hcl-keep-setup.git
cd ./hcl-keep-setup/infrastructure/secondary_local_auto/Linux_MacOS
chmod +x *.sh
```
- To init and start the container, run:
`sudo ./init_and_start.sh ID_NUM`

Replace `ID_NUM` with the required `server.id` number. 
`ID_NUM` ranges from 1 to 19.
- To remove the container, run:
`sudo ./down_rm.sh` (volume will be removed)
- To remove the container, run:
`sudo ./down.sh` (volume will be saved)
- To stop the container, run:
`sudo ./stop.sh`
- To start the container, run:
`sudo ./start.sh`
