SELECT
  count(distinct hr.id_analyses)
FROM
  heuristic_results as hr,classes as c
WHERE
  hr.id_heuristic_pattern IN
  (SELECT id from heuristic_pattern where heuristic_patterns.pattern LIKE '%Classloader%')
  AND hr.id_class = c.id AND c.id_packages NOT IN 
  ( SELECT distinct  (p.id)
    FROM packages as p,  saaf.ad_frameworks as ad    
    WHERE LOCATE(ad.path_fragment, p.name) != 0
  );
                
