import math, itertools

class Generate_Grid:
   test_list = []
   def __init__(self, alg, jobman, settings, params, target_points=30):
      self.test_list = []
      # Take the params listed in the params list,
      #given as tuples and then generate 20
      # equally spaced nodes from there.

      num_params = len(params)
      points_per_param = round(math.pow(target_points, 1.0 / num_params))

      # Now, for the number of params and their ranges, split into N
      # equally split sections
      ranges = []
      print settings[alg]
      for param in params:
         ranges.append(settings[alg][param + "_range"])

      # Calculate the list of points per param to test
      points = []
      for i in xrange(len(params)):
         low = ranges[i][0]
         high = ranges[i][1]
         step = (high - low) / float(points_per_param - 1)

         param_points = []
         for i in xrange(int(points_per_param)):
            val = low + step * i
            param_points.append(val)

         points.append(param_points)


      # Now, using the cartesian product, generate the list of
      # combinations of features
      test_list = []
      for c in itertools.product(points[0], points[1]):
         test_list.append(self.flatten(list(c)))
      print test_list
      for i in xrange(2,len(params)):
         temp_list = test_list[:]
         test_list = []
         for c in itertools.product(temp_list,points[i]):
            print self.flatten(list(c))
            test_list.append(self.flatten(list(c)))

      self.test_list = test_list



   def flatten(self,l):
     out = []
     for item in l:
       if isinstance(item, (list, tuple)):
         out.extend(self.flatten(item))
       else:
         out.append(item)
     return out


