import React from 'react';
import List from "@material-ui/core/es/List/List";
import Divider from "@material-ui/core/es/Divider/Divider";

const Menu = () => (
  <div className="menu">
    <List>{["item 1", "item 2"]}</List>
    <Divider />
    <List>{["item A, item B"]}</List>
  </div>
);

export default Menu;
