import os
import pathlib as plib
import time
from datetime import datetime
import click
import matplotlib.pyplot as plt
import numpy as np
import utils
import cp_optimizer
import ast



class PythonLiteralOption(click.Option):

    def type_cast_value(self, ctx, value):
        try:
            return ast.literal_eval(value)
        except:
            raise click.BadParameter(value)

@click.command()
@click.option(
    "--queries",
    cls=PythonLiteralOption,
    help="list of queries to be scheduled",
)
@click.option(
    "--num_partitions",
    default=16,
    type=int,
    help="number of partitions",
)
@click.option(
    "--probas",
    cls=PythonLiteralOption,
    help="probabilities list",
)
def optimize_schedule(queries, num_partitions, probas):
    # print("success")
    print(queries)
    print(num_partitions)
    # print(probas)
    print([float(i) for i in probas])
    print("done")


if __name__ == "__main__":
    # optimize_schedule()


    # runtime example without split#
    q_list = ["q1", "q3", "q5", "q4","q2", "q5", "q1"]
    res = utils.interpolate(utils.load(q_list, num_partitions='16'))
    precision = 1000
    C =16
    C_ = None # for split
    Q = len(q_list)
    R = int(round(Q / 2))
    # reg_factor = 0.001

    #with probas : make sure that length q_list = 2**x - 1
    path_sets_idx = utils.get_path_sets(range(len(q_list)))
    num_paths = len(path_sets_idx)
    probas = np.random.random(size=num_paths)
    probas = list(probas / sum(probas))
    probas_variables= None
    proba_variables = cp_optimizer.get_proba_variables(q_list, probas)

    process_time, r = cp_optimizer.compute_result(q_list, res, C, R, precision,
                                                        name_queries=True, C_=None, proba_variables=proba_variables)
    print("process time : ", process_time)
    if proba_variables is None:
        runtime, res_schedule = r
        print(runtime, res_schedule)
    else :
        cp_optimizer.print_proba_results(r)
        runtime, res_schedule, path_time, run_time, query_time = r




    # Split example #
    # q_list = ["q1", "q3", "q2", "q4","q2","q2", "q5", "q6"]
    # df_queries = utils.load(q_list, num_partitions='16')
    # res = utils.interpolate(df_queries)
    # precision = 10
    # C =16
    # Q = len(q_list)
    # R = int(round(Q / 2))
    # q_splitted, procees_time = cp_optimizer.split(q_list, res, C, precision)
    # results = []
    # for qq in q_splitted:
    #     results.append(cp_optimizer.optimize(qq, res, C, R, precision))
    #     print('done')
    # process_time, runtime, res_schedule = cp_optimizer.combine_results(results)
    # print(process_time, runtime, res_schedule)

