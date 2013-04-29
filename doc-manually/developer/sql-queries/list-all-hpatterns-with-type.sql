use saaf;
select 
    heuristic_pattern.id,
    heuristic_pattern.pattern,
    heuristic_pattern.description,
    enum_heuristic_pattern_searchin.name AS type
from
    heuristic_pattern,
    enum_heuristic_pattern_searchin
where
    heuristic_pattern.enum_searchin = enum_heuristic_pattern_searchin.enum;
