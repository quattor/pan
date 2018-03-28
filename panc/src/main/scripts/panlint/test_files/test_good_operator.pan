object template test_good_operator;

# See issue #187

type x = {
    'number' : long(-1..) = -1
};

prefix "/a";
"RealMemory" = to_long(total_ram() * 0.98) - 2;
"More" = 10 +
    to_long(total_ram(WORKER_NODES[0]) * 0.95) - 2048;

"b" = {
    for (idx = 31; idx >= 0; idx = idx - 1) {
        idx;
    };
};
