import requests
import itertools
import json
from bs4 import *

baseurl ="https://3029825460.cobaltmedia.xyz/"
jsonlist=[]

for i in itertools.count(1):
    print(f'On page {i}...')
    currenturl = f"{baseurl}anime-list.html?page={i}"
    page = requests.get(currenturl)
    soup = BeautifulSoup(page.content, 'html.parser')
    lis = soup.find('ul', class_='listing').find_all('li')

    if not lis:
        break

    for li in lis:
        x = BeautifulSoup(li.get('title'), 'html.parser')
        imagelink = x.find('img').get('src')
        animelink = baseurl + li.find('a').get('href')
        title = x.find_all('div')[1].find('a').text
        # print(title)
        # print(animelink)
        anime={
            "anime": {
                "Anime name" : title,
                "link" : animelink,
                "imagelink" : imagelink
            }
        }

        jsonlist.append(anime)

with open('animelist.json', 'w') as json_file:
    json.dump(jsonlist, json_file)