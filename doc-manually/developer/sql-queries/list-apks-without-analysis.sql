/*
    Find APK files that did not have an analysis;
*/
use saaf;
select 
    file_name
from
    apk_files
where
    apk_files.id not in (select distinct
            (id_apk)
        from
            analyses);
