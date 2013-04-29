/*
    Finds heuristic results that refer to a class 
    that is contained in a packages, whose name contains a patth fragement of known ad-frameworks
*/
SELECT 
    hr.id, hr.id_analyses, hr.id_heuristic_pattern, c.name as classname
from
    heuristic_results as hr,
    classes as c
WHERE
    hr.id_class = c.id AND c.id_packages IN (SELECT distinct
            (p.id)
        FROM
            packages as p,
            saaf.ad_frameworks as ad
        WHERE
            LOCATE(ad.path_fragment, p.name) != 0);
