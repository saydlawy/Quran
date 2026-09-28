#!/usr/bin/env python3
import json, os, hashlib, urllib.request, time
from concurrent.futures import ThreadPoolExecutor, as_completed

ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS=os.path.join(ROOT,"app","src","main","assets","quran")
DATA=os.path.join(ASSETS,"data"); FONTS=os.path.join(ASSETS,"fonts","qcf","v2")
API="https://api.quran.com/api/v4"; FONT="https://verses.quran.foundation/fonts/quran/hafs/v2/ttf"
UTHMANIC="https://verses.quran.foundation/fonts/quran/hafs/uthmanic_hafs/UthmanicHafs1Ver18.ttf"
PAGES=604; WORKERS=10
os.makedirs(DATA,exist_ok=True); os.makedirs(FONTS,exist_ok=True)

def fetch(url, attempts=6):
    last=None
    for n in range(attempts):
        try:
            req=urllib.request.Request(url,headers={"Accept":"application/json","User-Agent":"UltimateMushaf/1.0"})
            with urllib.request.urlopen(req,timeout=60) as r: return r.read()
        except Exception as e:
            last=e
            time.sleep(min(8,2**n))
    raise last

def page(p):
    path=os.path.join(DATA,f"raw_{p}.json")
    if not os.path.exists(path) or os.path.getsize(path)<100:
        url=f"{API}/verses/by_page/{p}?mushaf=1&words=true&word_fields=code_v2,text_uthmani,text_qpc_hafs,line_number,page_number,location,verse_key&per_page=all"
        open(path,"wb").write(fetch(url))
    return p

def font(p):
    path=os.path.join(FONTS,f"p{p}.ttf")
    if not os.path.exists(path) or os.path.getsize(path)<1000:
        open(path,"wb").write(fetch(f"{FONT}/p{p}.ttf"))
    return p

def main():
    os.makedirs(os.path.join(ASSETS,"fonts","uthmanic"),exist_ok=True)
    upath=os.path.join(ASSETS,"fonts","uthmanic","UthmanicHafs1Ver18.ttf")
    if not os.path.exists(upath) or os.path.getsize(upath)<1000:
        open(upath,"wb").write(fetch(UTHMANIC))
    jobs=[]
    with ThreadPoolExecutor(max_workers=WORKERS) as ex:
        for p in range(1,PAGES+1):
            jobs += [ex.submit(page,p),ex.submit(font,p)]
        for i,f in enumerate(as_completed(jobs),1):
            f.result()
            if i%50==0: print(f"assets {i}/{len(jobs)}",flush=True)

    pages={}; verses={}
    for p in range(1,PAGES+1):
        raw=json.load(open(os.path.join(DATA,f"raw_{p}.json"),encoding="utf-8"))
        obj={"page":p,"lines":{}}
        for v in raw.get("verses",[]):
            key=v["verse_key"]; sk,ak=map(int,key.split(":"))
            verses.setdefault(key,{"surah":sk,"ayah":ak,"page":p,"juz":v.get("juz_number",0)})
            for w in v.get("words",[]):
                if int(w.get("page_number") or p)!=p: continue
                glyph=w.get("code_v2") or ""
                if not glyph: continue
                line=int(w.get("line_number") or 1)
                obj["lines"].setdefault(str(line),[]).append({
                    "verse":key,"position":int(w.get("position") or 0),
                    "type":w.get("char_type_name") or "word",
                    "glyph":glyph,"text":w.get("text_uthmani") or w.get("text_qpc_hafs") or ""
                })
        for a in obj["lines"].values(): a.sort(key=lambda x:(x["position"],x["verse"]))
        pages[str(p)]=obj

    out=os.path.join(DATA,"quran_qcf_v2.json")
    with open(out,"w",encoding="utf-8") as f:
        json.dump({"schema":2,"mushaf_id":1,"edition":"QCF V2 / Madinah 604","pages":pages,"verses":verses},f,ensure_ascii=False,separators=(",",":"))
    for p in range(1,PAGES+1): os.remove(os.path.join(DATA,f"raw_{p}.json"))
    sha=hashlib.sha256(open(out,"rb").read()).hexdigest()
    with open(os.path.join(ASSETS,"MANIFEST.json"),"w",encoding="utf-8") as f:
        json.dump({"schema":2,"mushaf_id":1,"pages":604,"lines_per_page":15,"ayahs":6236,"data_sha256":sha,"source":"Quran Foundation content API v4 + Quran Foundation QCF V2/Uthmanic Hafs fonts"},f,ensure_ascii=False,indent=2)
    print("QCF V2 asset generation PASS",sha)

if __name__=="__main__": main()
