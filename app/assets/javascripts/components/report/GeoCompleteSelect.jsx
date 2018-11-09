import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Cesium from "cesium/Cesium";
import Select from "react-select";
import Paper from "@material-ui/core/Paper";
import Typography from "@material-ui/core/Typography";

const styles = theme => ({
  noOptionsMessage: {
    padding: `${theme.spacing.unit}px ${theme.spacing.unit * 2}px`,
  },
  locationMenuPaper: {
    position: 'absolute',
    zIndex: 1,
    marginTop: theme.spacing.unit,
    left: 0,
    right: 0,
  },
});

const MIN_INPUT_LENGTH = 4;

class GeoCompleteSelect extends React.Component {
  constructor(props) {
    super(props);

    this.handleLocationChange = this.handleLocationChange.bind(this);
    this.handleLocationSelect = this.handleLocationSelect.bind(this);

    this.selectComponents = {
      NoOptionsMessage: props => {
        return (
          <Typography
            color="textSecondary"
            className={props.selectProps.classes.noOptionsMessage}
            {...props.innerProps}
          >
            No matching locations
          </Typography>
        );
      },
      Menu: props => {
        const {inputValue} = props.selectProps;
        return !inputValue || inputValue.length < MIN_INPUT_LENGTH ? null : (
          <Paper square className={props.selectProps.classes.locationMenuPaper} {...props.innerProps}>
            {props.children}
          </Paper>
        );
      },
    };

    this.state = {
      locationOptions: [],
    };
  }

  handleLocationChange(value) {
    if (!value || value.length < MIN_INPUT_LENGTH) return;

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

    this.props.onSelect();
  }

  render() {
    let { classes } = this.props;
    let { NoOptionsMessage, Menu } = this.selectComponents;

    return (
      <Select
        classes={classes}
        onInputChange={this.handleLocationChange}
        onChange={this.handleLocationSelect}
        options={this.state.locationOptions}
        placeholder="Location"
        components={{ NoOptionsMessage, Menu }}
      />
    );
  }
}

GeoCompleteSelect.propTypes = {
  classes: PropTypes.object.isRequired,
  controller: PropTypes.object,
  onSelect: PropTypes.func.isRequired,
};

export default withStyles(styles)(GeoCompleteSelect);