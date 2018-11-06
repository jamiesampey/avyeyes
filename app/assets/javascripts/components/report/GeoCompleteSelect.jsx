import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Cesium from "cesium/Cesium";
import Select from "react-select";
import Paper from "@material-ui/core/Paper/Paper";

const styles = theme => ({
  locationMenuPaper: {
    position: 'absolute',
    zIndex: 1,
    marginTop: theme.spacing.unit,
    left: 0,
    right: 0,
  },
});

class GeoCompleteSelect extends React.Component {
  constructor(props) {
    super(props);
    this.handleLocationChange = this.handleLocationChange.bind(this);
    this.handleLocationSelect = this.handleLocationSelect.bind(this);

    this.state = {
      locationOptions: [],
    };
  }

  handleLocationChange(value) {
    if (!value || value.length < 4) return;

    this.props.controller.geocode(value).then( geocodeResults => {
      this.setState({
        locationOptions: geocodeResults.map(result => {
          return {
            label: result.displayName,
            value: JSON.stringify(result.destination),
          }
        })
      })
    });
  }

  handleLocationSelect(selected) {
    let location = JSON.parse(selected.value);
    let dest = location.x ? new Cesium.Cartesian3(location.x, location.y, location.z):
      new Cesium.Rectangle(location.west, location.south, location.east, location.north);
    this.props.controller.flyToDest(dest);

    this.props.callback();
  }

  render() {
    let { classes } = this.props;

    const Menu = (props) => {
      return (
        <Paper square className={props.selectProps.classes.locationMenuPaper} {...props.innerProps}>
          {props.children}
        </Paper>
      );
    };

    return (
      <Select
        classes={classes}
        onInputChange={this.handleLocationChange}
        onChange={this.handleLocationSelect}
        options={this.state.locationOptions}
        placeholder="Location"
        components={{Menu}}
      />
    );
  }
}

GeoCompleteSelect.propTypes = {
  classes: PropTypes.object.isRequired,
  controller: PropTypes.object,
  callback: PropTypes.func.isRequired,
};

export default withStyles(styles)(GeoCompleteSelect);