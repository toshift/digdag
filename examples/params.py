from __future__ import print_function
import digdag

def simple(data, number):
    print("simple: data = "+data)
    print("simple: number = "+number)

def export_params_step1(mysql):
    print("export_params_step1: mysql = "+str(mysql))

def export_params_step2(mysql, table):
    print("export_params_step2: mysql = "+str(mysql))
    print("export_params_step2: table = "+table)

def export_params_step3(mysql):
    print("export_params_step3: mysql = "+str(mysql))

def export_overwrite(mysql):
    print("export_overwrite: mysql = "+str(mysql))

def set_export():
    digdag.env.export({"table": "carried information"})

def show_export(table=None):
    print("show_export: table = " + str(table))

