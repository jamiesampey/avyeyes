import React, {Component} from 'react';
import ReactDOM from "react-dom";
import Greeter from "./Greeter";

import '../stylesheets/style.scss';

class AvyEyesClient extends Component {
    render() {
        return (
            <div className="AvyEyesClient">
                <Greeter name="Little Razzy" />
            </div>
        )
    }
}

ReactDOM.render(<AvyEyesClient/>, document.getElementById('root'));