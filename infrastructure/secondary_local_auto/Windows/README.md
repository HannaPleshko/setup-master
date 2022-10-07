- Сlone this repository
```
git clone git@code.xby.icdc.io:hcl/quattro/hcl-keep-setup.git
cd hcl-keep-setup\infrastructure\secondary_local_auto/Windows
```
- To init and start the container, run:
`init_and_start.bat ID_NUM`

Replace `ID_NUM` with the required `server.id` number. 
`ID_NUM` ranges from 1 to 19.
- To remove the container, run:
`down_rm.bat` (volume will be removed)
- To remove the container, run:
`down.bat` (volume will be saved)
- To stop the container, run:
`stop.bat`
- To start the container, run:
`start.bat`
