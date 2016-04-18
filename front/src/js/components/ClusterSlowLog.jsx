var React = require('react');

var ClusterActions = require('../actions/ClusterActions');
var NodeSlowLogQueryStore = require('../stores/NodeSlowLogQueryStore');
var NodeSlowLogsStore = require('../stores/NodeSlowLogsStore');
var Utils = require('../utils/Utils');

var ClusterSlowLogs = React.createClass({
    getInitialState: function() {
        return {
            slowlogs: NodeSlowLogsStore.getSlowLogs(this.props.cluster.cluster_name),
            loading: true,
            fullViewConfig: {}
        };
    },
    componentDidMount: function() {
        var _this = this;
        var query = NodeSlowLogQueryStore.getNodeSlowLogQuery(this.props.cluster.cluster_name);

        NodeSlowLogsStore.addChangeListener(this.handleChangeNodeSlowLog);
        NodeSlowLogQueryStore.addChangeListener(this.handleChangeQuery);

        ClusterActions.getNodeSlowLogs(
            this.props.cluster.cluster_name,
            {
                nodes: query.nodes
            },
            {
                complete: function() {
                    _this.setState({loading: false});
            }
        });
    },
    componentWillUnmount: function() {
      NodeSlowLogsStore.removeChangeListener(this.handleChangeNodeSlowLog);
      NodeSlowLogQueryStore.removeChangeListener(this.handleChangeQuery);
    },
    render: function() {
      console.log(123)
        return (
            <div className="panel panel-default cluster-nodes-slowlogs-components">
                <div className="panel-heading clearfix">
                    Queries
                </div>

                <div className="panel-body">
                    <div className="table-responsive">
                        <table className="table table-striped">
                            <thead>
                                <tr>
                                    <th><span data-toggle="tooltip" data-placement="top" title="">host:port</span></th>
                                    <th><span data-toggle="tooltip" data-placement="top" title="">slowlog id</span></th>
                                    <th><span data-toggle="tooltip" data-placement="top" title="">time stamp</span></th>
                                    <th><span data-toggle="tooltip" data-placement="top" title="">execution time</span></th>
                                    <th><span data-toggle="tooltip" data-placement="top" title="">args</span></th>
                                </tr>
                            </thead>
                            <tbody>
                            {
                                _.map(this.state.slowlogs, function(slowlogs, nodeid) {
                                  return (
                                    _.map(slowlogs, function(slowlog) {
                                      return (
                                        <tr>
                                          <td>{nodeid}</td>
                                          <td>{slowlog.id}</td>
                                          <td>{slowlog.time_stamp}</td>
                                          <td>{slowlog.execution_time}</td>
                                          <td>{slowlog.args ? slowlog.args.join(' ') : null}</td>
                                        </tr>
                                      )
                                    })
                                  )
                                })
                            }
                            </tbody>
                        </table>

                    </div>
                </div>
            </div>
        );
    },
    handleChangeNodeSlowLog: function() {
      console.log(999)
        this.setState({
            slowlogs: NodeSlowLogsStore.getSlowLogs(this.props.cluster.cluster_name)
        });
    },
    handleChangeQuery: function() {
        console.log(888)
        var _this = this;
        var query = NodeSlowLogQueryStore.getNodeSlowLogQuery(this.props.cluster.cluster_name);

        var requestData = {
            start: query.start,
            end: query.end,
            nodes: query.nodes
        };

        this.setState({
            loading: true,
            query: query
        });
        // ClusterActions.getNodeSlowLogs(
        //     this.props.cluster.cluster_name,
        //     requestData,
        //     {
        //         complete: function() {
        //             _this.setState({loading: false});
        //         }
        //     }
        // );
    },
});

module.exports = ClusterSlowLogs;
