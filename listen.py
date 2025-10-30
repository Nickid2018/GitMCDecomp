from time import sleep
from datetime import datetime

import requests

session = requests.Session()
# session.proxies = {"http": "<PROXY>", "https": "<PROXY>"}

lastModified = None
while True:
    try:
        headers = {}
        if lastModified:
            headers['If-Modified-Since'] = lastModified
        resp = session.head('https://piston-meta.mojang.com/mc/game/version_manifest.json', headers=headers)
        if resp.status_code != 304:
            lastModified = resp.headers.get('Last-Modified')
            ret = session.post(
                "https://api.github.com/repos/<USER>/<REPO>/actions/workflows/listen-mc-decompile.yml/dispatches",
                headers={
                    "Accept": "application/vnd.github.v3+json",
                    "Authorization": "Bearer <TOKEN>",
                },
                data='{"ref": "master"}',
            )
            print(f"Manifest modified, call program at {datetime.now().isoformat()}")
            sleep(360)
        else:
            sleep(60)
    except Exception as e:
        print(e)
