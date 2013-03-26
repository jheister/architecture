var Graphing = {}; (function() {
  Graphing.Graph = function(dragCallback) {
    var stage, nodeLayer, edgeLayer, nodes = {}, edges = {};

    return {
      render: function() {
        stage = new Kinetic.Stage({
                      container: 'graphContainer',
                      width: 800,
                      height: 800
                    });


        edgeLayer = new Kinetic.Layer();
        nodeLayer = new Kinetic.Layer();
        stage.add(edgeLayer);
        stage.add(nodeLayer);
      },
      addNode: function(prop) {
        var cell = new Graphing.Cell(nodeLayer, prop);
        cell.addDragCallback(dragCallback)
        nodes[prop.name] = cell;
        cell.render();
      },
      addEdge: function(prop) {
        var edge = new Graphing.Edge(edgeLayer, nodes[prop.from], nodes[prop.to], prop.weight);
        edges[prop.from + prop.to] = edge;
        edge.render();
      },
      updateEdge: function(prop) {
        edges[prop.from + prop.to].update(prop.weight);
      }
    }
  }

  Graphing.Cell = function(layer, properties) {
    var group, box, text, edges = Array(), dragCallbacks = Array();

    return {
        render: function() {
          group = new Kinetic.Group({ x: properties.x,
                                      y: properties.y,
                                      draggable: true });

          box = new Kinetic.Rect({
                          x: 0,
                          y: 0,
                          width: 100,
                          height: 50,
                          stroke: '#555',
                          strokeWidth: 5,
                          fill: '#ddd',
                          shadowColor: 'black',
                          shadowBlur: 10,
                          shadowOffset: [10, 10],
                          shadowOpacity: 0.2,
                          cornerRadius: 10
                        });
           group.on('dragend', function() { dragCallbacks.forEach(function(callback) { callback({name: properties.name, coordinates: {x: group.getX(), y: group.getY()}}); })});
           group.on('dragstart dragmove', function() {
             edges.forEach(function(edge) { edge(); });
           });
          group.add(box);


          text = new Kinetic.Text({
                         x: 0,
                         y: 0,
                         text: properties.name,
                         fontSize: 20,
                         fontFamily: 'Calibri',
                         fill: '#555',
                         width: 100,
                         padding: 15,
                         align: 'center'
                       });

          group.add(text);
          layer.add(group);

          layer.draw();
        },
        getX: function() { return group.getX(); },
        getY: function() { return group.getY(); },
        xCenter: function() { return group.getX() + 50 },
        bottom: function() { return group.getY() + 50 + 10},
        top: function() { return group.getY() - 10},
        right: function() { return group.getX() + 100 + 10},
        yCenter: function() { return group.getY() + 25 },
        left: function() { return group.getX() - 10 },
        addEdge: function(edge) {
          edges.push(edge);
        },
        addDragCallback: function(callback) {
          dragCallbacks.push(callback);
        },
        getCoordinates: function() {
          return new Graphing.Coordinates(group.getX(), group.getY());
        }
    }
  }

  Graphing.Coordinates = function(x, y) {
    return {
      minus: function(coordinates) {
        return new Graphing.Coordinates(x - coordinates.getX(), y - coordinates.getY());
      },
      getX: function() {
        return x;
      },
      getY: function() {
        return y;
      },
      angle: function() {
        var theAngle = Math.atan(y / x) * (180/Math.PI);

        if (x < 0) {
          theAngle = theAngle + 180;
        }
        theAngle = (theAngle + 360) % 360;
        return theAngle;
      }
    }
  }

  Graphing.Edge = function(layer, from, to, weight) {
    var line, arrowHead, update, figureOutStartAndEnd, angle;

    update = function() {
      var points = figureOutStartAndEnd();
      line.setPoints(points);
      arrowHead.setX(points[2]);
      arrowHead.setY(points[3]);

      arrowHead.setRotationDeg(Graphing.Coordinates(points[2], points[3]).minus(Graphing.Coordinates(points[0], points[1])).angle() + 90);
      layer.draw();
    }

    figureOutStartAndEnd = function() {
    var theAngle = angle();

    if (((theAngle > 45) && (theAngle < 135)) || ((theAngle > 225) && (theAngle < 315))) {
      if (from.bottom() < to.top()) {
        return [from.xCenter(), from.bottom(), to.xCenter(), to.top()];
      } else if(from.top() >  to.bottom()) {
        return [from.xCenter(), from.top(), to.xCenter(), to.bottom()];
      } else {
        return [0,0,0,0];
      }
    } else {
      if (from.right() < to.left()) {
        return [from.right(), from.yCenter(), to.left(), to.yCenter()];
      } else if (from.left() > to.right()) {
        return [from.left(), from.yCenter(), to.right(), to.yCenter()];
      } else {
        return [0,0,0,0];
      }
    }
  }

  angle = function() {
    return to.getCoordinates().minus(from.getCoordinates()).angle();
  }

    from.addEdge(update);
    to.addEdge(update);

    return {
      render: function() {
        var points = figureOutStartAndEnd();

        line = new Kinetic.Line({
                           points: points,
                           stroke: '#555',
                           strokeWidth: (5 + weight),
                           lineCap: 'round',
                           lineJoin: 'round'
                           });

        arrowHead = new Kinetic.RegularPolygon({
                x: points[2],
                y: points[3],
                sides: 3,
                radius: (10 + weight),
                stroke: '#555',
                strokeWidth: 5,
                fill: '#555',
                shadowColor: 'black',
                shadowBlur: 10,
                shadowOffset: [10, 10],
                shadowOpacity: 0.2,
                cornerRadius: 10
              });

        arrowHead.setRotationDeg(Graphing.Coordinates(points[2], points[3]).minus(Graphing.Coordinates(points[0], points[1])).angle() + 90);

        layer.add(line);
        layer.add(arrowHead);
        layer.draw();
      },
      update: function(newWeight) {
        line.setStrokeWidth(5 + newWeight);
        arrowHead.setRadius(10 + newWeight);
        layer.draw();
      }
    }
  }
})();