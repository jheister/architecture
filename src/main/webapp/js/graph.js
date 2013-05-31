var Graphing = {}; (function() {
  Graphing.Graph = function(dragCallback, width, height, graphContainer) {
    var stage, nodeLayer, edgeLayer, nodes = {}, edges = {};

    return {
      render: function() {
        stage = new Kinetic.Stage({
                      container: graphContainer,
                      width: width,
                      height: height
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
    var group, box, text, edges = Array(), dragCallbacks = Array(), zoom;

    return {
        render: function() {
          group = new Kinetic.Group({ x: properties.x,
                                      y: properties.y,
                                      draggable: true });

          box = new Kinetic.Rect({
                          x: 0,
                          y: 0,
                          width: properties.width,
                          height: properties.height,
                          stroke: '#555',
                          strokeWidth: 5,
                          fill: '#ddd',
                          shadowColor: 'black',
                          shadowBlur: 10,
                          shadowOffset: [10, 10],
                          shadowOpacity: 0.2,
                          cornerRadius: 10
                        });

          zoom = new Kinetic.Rect({
                                   x: 0,
                                   y: 0,
                                   width: 20,
                                   height: properties.height,
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

           zoom.on('mouseup', properties.onClick)

          group.add(box);

          text = new Kinetic.Text({
                         x: 0,
                         y: 0,
                         text: properties.name,
                         fontSize: 20,
                         fontFamily: 'Calibri',
                         fill: '#555',
                         width: properties.width,
                         padding: 15,
                         align: 'center'
                       });

          group.add(text);
          group.add(zoom);

          layer.add(group);

          layer.draw();
        },
        getX: function() { return group.getX(); },
        getY: function() { return group.getY(); },
        xCenter: function() { return group.getX() + (properties.width/2) },
        bottom: function() { return group.getY() + properties.height},
        top: function() { return group.getY()},
        right: function() { return group.getX() + properties.width},
        yCenter: function() { return group.getY() + (properties.height / 2) },
        left: function() { return group.getX()},
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
      plus: function(coordinates) {
        return new Graphing.Coordinates(x + coordinates.getX(), y + coordinates.getY());
      },
      getX: function() {
        return x;
      },
      getY: function() {
        return y;
      },
      divideBy: function(d) {
        return new Graphing.Coordinates(x / d, y / d)
      },
      times: function(d) {
        return new Graphing.Coordinates(x * d, y * d);
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
    var line, arrowHead, update, figureOutStartAndEnd, figureOutStartAndEnd1, angle, text;

    update = function() {
      var points = figureOutStartAndEnd();
      line.setPoints(points);
      arrowHead.setX(points[2]);
      arrowHead.setY(points[3]);
      text.setX(((points[2] -points[0]) / 2) + points[0]);
      text.setY(((points[3] - points[1]) / 2) + points[1])

      arrowHead.setRotationDeg(Graphing.Coordinates(points[2], points[3]).minus(Graphing.Coordinates(points[0], points[1])).angle() + 90);
      layer.draw();
    }

    figureOutStartAndEnd = function() {
      var points = figureOutStartAndEnd1();

      var start = new Graphing.Coordinates(points[2], points[3]).minus(new Graphing.Coordinates(points[0], points[1])).divideBy(10).plus(new Graphing.Coordinates(points[0], points[1]));
      var end = new Graphing.Coordinates(points[2], points[3]).minus(new Graphing.Coordinates(points[2], points[3]).minus(new Graphing.Coordinates(points[0], points[1])).divideBy(10));

      return [start.getX(), start.getY(), end.getX(), end.getY()];
    }

    figureOutStartAndEnd1 = function() {
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

        arrowHead = new Kinetic.Polygon({
                points: [points[2], points[3], points[2] + 5, points[3] + 10, points[2] - 5, points[3] + 10],
                stroke: '#555',
                strokeWidth: (5 + weight),
                fill: '#555',
                shadowColor: 'black',
                shadowBlur: 10,
                shadowOffset: [10, 10],
                shadowOpacity: 0.2,
                cornerRadius: 10,
                offset: [points[2], points[3]]
              });
        arrowHead.setX(points[2]);
        arrowHead.setY(points[3]);

       text = new Kinetic.Text({
                      x: ((points[2] -points[0]) / 2) + points[0],
                      y: ((points[3] - points[1]) / 2) + points[1],
                      text: weight,
                      fontSize: 20,
                      fontFamily: 'Calibri',
                      fill: '#555',
                      width: 50,
                      padding: 15,
                      align: 'center'
                    });

        arrowHead.setRotationDeg(Graphing.Coordinates(points[2], points[3]).minus(Graphing.Coordinates(points[0], points[1])).angle() + 90);

        layer.add(line);
        layer.add(arrowHead);
        layer.add(text);
        layer.draw();
      },
      update: function(newWeight) {
        line.setStrokeWidth(5 + newWeight);
        arrowHead.setStrokeWidth(5 + newWeight);
        text.setText(newWeight);
        layer.draw();
      }
    }
  }
})();